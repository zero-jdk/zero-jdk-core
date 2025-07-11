package dev.zerojdk.domain.service.catalog.storage;

import dev.zerojdk.domain.port.out.event.DomainEventPublisher;
import dev.zerojdk.domain.port.out.catalog.client.RemoteReleaseClient;
import dev.zerojdk.domain.model.catalog.client.Asset;
import dev.zerojdk.domain.model.catalog.client.Release;
import dev.zerojdk.domain.model.catalog.Catalog;
import dev.zerojdk.domain.service.catalog.CatalogUnchangedException;
import dev.zerojdk.domain.service.unarchiving.ArchiveExtractionService;
import dev.zerojdk.domain.service.unarchiving.ExtractedArtifact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoteCatalogProviderServiceTest {
    @Mock
    private RemoteReleaseClient remoteReleaseClient;
    @Mock
    private ArchiveExtractionService archiveExtractionService;
    @Mock
    private DomainEventPublisher domainEventPublisher;
    @InjectMocks
    private RemoteCatalogProviderService service;

    @TempDir
    Path tempDir;

    @Test
    void provideLatestReturnsCatalogFromLatestRelease() throws IOException {
        // Given
        Asset asset = new Asset("https://foo.zip");
        File archive = new File("foo.zip");

        // Create the expected directory structure: root -> subdirectory -> catalog.json
        Path extractedRoot = tempDir.resolve("extracted");
        Path catalogDir = extractedRoot.resolve("catalog-dir");
        Files.createDirectories(catalogDir);
        Path catalogFile = Files.createFile(catalogDir.resolve("catalog.json"));

        when(remoteReleaseClient.getLatestRelease())
            .thenReturn(new Release("v1.2.3", List.of(asset)));
        when(remoteReleaseClient.downloadReleaseAsset(eq(asset), any()))
            .thenReturn(archive);

        when(archiveExtractionService.unarchive(eq(archive), any()))
            .thenReturn(new ExtractedArtifact(extractedRoot, null));

        // When
        Catalog result = service.provideLatest();

        // Then
        assertThat(result.version())
            .isEqualTo("v1.2.3");
        assertThat(result.location())
            .isEqualTo(catalogFile);
    }

    @Test
    void downloadThrowsWhenUnexpectedFileStructure() {
        // Given
        Asset asset = new Asset("https://foo.zip");
        File archive = new File("foo.zip");

        when(remoteReleaseClient.getLatestRelease())
            .thenReturn(new Release("v9.9.9", List.of(asset)));
        when(remoteReleaseClient.downloadReleaseAsset(eq(asset), any()))
            .thenReturn(archive);

        // Return an empty directory (no subdirectories) to trigger "Unexpected file structure"
        when(archiveExtractionService.unarchive(eq(archive), any()))
            .thenReturn(new ExtractedArtifact(tempDir, null));

        // Then
        assertThatThrownBy(() -> service.provideLatest())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Unexpected file structure");
    }

    @Test
    void provideLatestIfNewerThrowsWhenSameVersion() {
        // Given
        Asset asset = new Asset("https://foo.zip");

        Release release = new Release("v1.2.3", List.of(asset));
        when(remoteReleaseClient.getLatestRelease())
            .thenReturn(release);

        // Then / Then
        assertThatThrownBy(() -> service.provideLatestIfNewer("v1.2.3"))
            .isInstanceOf(CatalogUnchangedException.class);
    }

    @Test
    void downloadThrowsIfNoAssetFound() {
        // Given
        Release release = new Release("v1.0.0", List.of());
        when(remoteReleaseClient.getLatestRelease()).thenReturn(release);

        // Then
        assertThatThrownBy(() -> service.provideLatest())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Asset not found");
    }
}
