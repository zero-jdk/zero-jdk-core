package dev.zerojdk.domain.service.catalog.storage;

import dev.zerojdk.domain.model.catalog.storage.event.CatalogDownloadCompleted;
import dev.zerojdk.domain.model.catalog.storage.event.CatalogDownloadFailed;
import dev.zerojdk.domain.model.catalog.storage.event.CatalogDownloadProgress;
import dev.zerojdk.domain.model.catalog.storage.event.CatalogDownloadStarted;
import dev.zerojdk.domain.model.catalog.client.Asset;
import dev.zerojdk.domain.port.out.catalog.client.RemoteReleaseClient;
import dev.zerojdk.domain.model.catalog.client.Release;
import dev.zerojdk.domain.model.catalog.Catalog;
import dev.zerojdk.domain.port.out.catalog.CatalogProviderService;
import dev.zerojdk.domain.port.out.event.DomainEventPublisher;
import dev.zerojdk.domain.service.catalog.CatalogUnchangedException;
import dev.zerojdk.domain.service.unarchiving.ExtractedArtifact;
import dev.zerojdk.domain.service.unarchiving.ArchiveExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class RemoteCatalogProviderService implements CatalogProviderService {
    private final RemoteReleaseClient remoteReleaseClient;
    private final ArchiveExtractionService archiveExtractionService;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public Catalog provideLatest() {
        Release latestRelease = remoteReleaseClient.getLatestRelease();
        return download(latestRelease);
    }

    @Override
    public Catalog provideLatestIfNewer(String currentVersion) {
        Release latestRelease = remoteReleaseClient.getLatestRelease();
        String version = latestRelease.tagName();

        if (currentVersion != null && currentVersion.equals(version)) {
            throw new CatalogUnchangedException("Catalog is already up-to-date");
        }

        return download(latestRelease);
    }

    @SneakyThrows
    private Catalog download(Release latestRelease) {
        Asset asset = latestRelease.assets().stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Asset not found"));

        File downloaded = downloadReleaseAsset(latestRelease, asset);
        Path extracted = extractArchive(downloaded);
        Path catalogPath = locateCatalogFile(extracted);

        return new Catalog(latestRelease.tagName(), catalogPath);
    }

    /**
     * Locates the catalog file after extracting the archive.
     *
     * <p>
     * Assumptions:
     * The extracted path contains a directory with one file named catalog.json
     * </p>
     *
     * To make the implicit coupling between {@code RemoteReleaseClient} (which provides the asset)
     * and {@code ArchiveExtractionService} (which extracts it) visible, this service is responsible
     * for asserting the expected structure and content of the archive.
     *
     * @return The path to the catalog file
     */
    private Path locateCatalogFile(Path root) throws IOException {
        try (Stream<Path> dirs = Files.list(root).filter(Files::isDirectory)) {
            return dirs.findFirst()
                .flatMap(firstDir -> {
                    try (Stream<Path> files = Files.list(firstDir).filter(Files::isRegularFile)) {
                        return files.findFirst();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }).orElseThrow(() -> new RuntimeException("Unexpected file structure"));
        }
    }

    private File downloadReleaseAsset(Release release, Asset asset) {
        try {
            domainEventPublisher.publish(new CatalogDownloadStarted());
            File downloadReleaseAsset = remoteReleaseClient.downloadReleaseAsset(asset, (bytesRead, totalBytes) ->
                new CatalogDownloadProgress(release.tagName(), bytesRead, totalBytes));
            domainEventPublisher.publish(new CatalogDownloadCompleted());

            return downloadReleaseAsset;
        } catch (Exception e) {
            domainEventPublisher.publish(new CatalogDownloadFailed(e));
            throw e;
        }
    }

    private Path extractArchive(File archive) throws IOException {
        Path tempExtractDir = Files.createTempDirectory("catalog-extract-");
        ExtractedArtifact extractedArtifact = archiveExtractionService.unarchive(archive, tempExtractDir);

        return extractedArtifact.getPath();
    }
}