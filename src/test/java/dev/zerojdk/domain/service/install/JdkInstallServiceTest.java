package dev.zerojdk.domain.service.install;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.model.OperatingSystem;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.model.ProcessorArchitecture;
import dev.zerojdk.domain.model.release.InstallationRecord;
import dev.zerojdk.domain.model.release.JdkRelease;
import dev.zerojdk.domain.model.release.events.download.JdkDownloadCompleted;
import dev.zerojdk.domain.model.release.events.download.JdkDownloadFailed;
import dev.zerojdk.domain.model.release.events.download.JdkDownloadStarted;
import dev.zerojdk.domain.model.release.events.installation.JdkInstallationCompleted;
import dev.zerojdk.domain.model.release.events.installation.JdkInstallationFailed;
import dev.zerojdk.domain.model.release.events.installation.JdkInstallationStarted;
import dev.zerojdk.domain.port.out.download.DownloadService;
import dev.zerojdk.domain.port.out.event.DomainEventPublisher;
import dev.zerojdk.domain.port.out.layout.JdkReleaseLayout;
import dev.zerojdk.domain.port.out.release.JdkInstaller;
import dev.zerojdk.domain.service.catalog.CatalogService;
import dev.zerojdk.domain.service.config.UnsupportedIdentifierException;
import dev.zerojdk.domain.service.release.JdkReleaseService;
import dev.zerojdk.domain.service.unarchiving.ArchiveExtractionService;
import dev.zerojdk.domain.service.unarchiving.ExtractedArtifact;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.throwable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JdkInstallServiceTest {
    @Mock
    private DomainEventPublisher publisher;
    @Mock
    private JdkReleaseLayout layout;
    @Mock
    private DownloadService downloadService;
    @Mock
    private ArchiveExtractionService extractor;
    @Mock
    private CatalogService catalog;
    @Mock
    private JdkInstaller installer;
    @Mock
    private JdkReleaseService releaseService;
    @InjectMocks
    private JdkInstallService service;

    @Test
    void installsNewJdkRelease() throws IOException, InterruptedException {
        // Given
        Platform platform = new Platform(OperatingSystem.LINUX, ProcessorArchitecture.AARCH64);

        JdkVersion version = new JdkVersion();
        version.setIdentifier("temurin-21");

        File archive = new File("dummy.tar.gz");
        Path temp = Path.of("/tmp/extracted");
        InstallationRecord record = new InstallationRecord(
            version.getIdentifier(), Path.of("/install"), Path.of("/install/java"));

        ExtractedArtifact extractedArtifact = mock(ExtractedArtifact.class);

        when(catalog.findByIdentifier(platform, version.getIdentifier()))
            .thenReturn(Optional.of(version));
        when(releaseService.findJdkRelease(version))
            .thenReturn(Optional.empty());
        when(downloadService.download(any(), any()))
            .thenReturn(archive);
        when(layout.tempDirectory(version))
            .thenReturn(temp);
        when(extractor.unarchive(archive, temp))
            .thenReturn(extractedArtifact);
        when(installer.install(version, extractedArtifact))
            .thenReturn(record);

        // When
        InstallationRecord actual = service.install(platform, version.getIdentifier());

        // Then
        assertThat(actual).isEqualTo(record);

        verify(extractedArtifact).delete();
    }

    @Test
    void publishesEventsOnSuccessfulDownload() throws IOException, InterruptedException {
        // Given
        Platform platform = new Platform(OperatingSystem.LINUX, ProcessorArchitecture.AARCH64);

        JdkVersion version = new JdkVersion();
        version.setIdentifier("temurin-21");

        File archive = new File("dummy.tar.gz");
        Path temp = Path.of("/tmp/extracted");
        InstallationRecord record = new InstallationRecord("temurin-21", Path.of("/install"), Path.of("/install/java"));
        ExtractedArtifact extractedArtifact = mock(ExtractedArtifact.class);

        when(catalog.findByIdentifier(platform, version.getIdentifier()))
            .thenReturn(Optional.of(version));
        when(releaseService.findJdkRelease(version))
            .thenReturn(Optional.empty());
        when(downloadService.download(any(), any()))
            .thenReturn(archive);
        when(layout.tempDirectory(version))
            .thenReturn(temp);
        when(extractor.unarchive(archive, temp))
            .thenReturn(extractedArtifact);
        when(installer.install(version, extractedArtifact))
            .thenReturn(record);

        // When
        service.install(platform, version.getIdentifier());

        // Then
        verify(publisher)
            .publish(new JdkDownloadStarted(version));
        verify(publisher)
            .publish(new JdkDownloadCompleted(version));
    }

    @Test
    void publishesEventsOnDownloadFailure() throws IOException, InterruptedException {
        // Given
        Platform platform = new Platform(OperatingSystem.LINUX, ProcessorArchitecture.AARCH64);

        JdkVersion version = new JdkVersion();
        version.setIdentifier("temurin-21");

        when(catalog.findByIdentifier(platform, version.getIdentifier()))
            .thenReturn(Optional.of(version));
        when(releaseService.findJdkRelease(version))
            .thenReturn(Optional.empty());

        RuntimeException downloadError = new RuntimeException("network error");
        when(downloadService.download(any(), any()))
            .thenThrow(downloadError);

        // When / Then
        Assertions.assertThatThrownBy(() -> service.install(platform, version.getIdentifier()))
            .isInstanceOf(JdkDownloadFailedException.class)
            .asInstanceOf(throwable(JdkDownloadFailedException.class))
            .extracting(JdkDownloadFailedException::getJdkVersion)
            .isEqualTo(version);

        verify(publisher)
            .publish(new JdkDownloadStarted(version));
        verify(publisher)
            .publish(argThat(event ->
                event instanceof JdkDownloadFailed(JdkVersion v, Exception cause)
                    && v.equals(version)
                    && cause == downloadError));
    }

    @Test
    void returnExistingIfAlreadyInstalled() {
        // Given
        Platform platform = new Platform(OperatingSystem.LINUX, ProcessorArchitecture.AARCH64);

        JdkVersion version = new JdkVersion();
        version.setIdentifier("temurin-21");

        Path installRoot = Path.of("temurin-21");
        Path javaHome = Path.of("temurin-21/bin/java");

        JdkRelease jdkRelease = new JdkRelease(version, installRoot, javaHome);

        when(catalog.findByIdentifier(platform, version.getIdentifier()))
            .thenReturn(Optional.of(version));
        when(releaseService.findJdkRelease(version))
            .thenReturn(Optional.of(jdkRelease));

        // When
        InstallationRecord actual = service.install(platform, version.getIdentifier());

        // Then
        assertThat(actual)
            .isEqualTo(new InstallationRecord(jdkRelease.jdkVersion().getIdentifier(), installRoot, javaHome));
    }

    @Test
    void throwsIfIdentifierUnknown() {
        // Given
        Platform platform = new Platform(OperatingSystem.LINUX, ProcessorArchitecture.AARCH64);

        // When / Then
        assertThrows(UnsupportedIdentifierException.class, () ->
            service.install(platform, "does-not-exist"));
    }

    @Test
    void publishesEventsOnSuccessfulInstall() throws IOException, InterruptedException {
        // Given
        Platform platform = new Platform(OperatingSystem.LINUX, ProcessorArchitecture.AARCH64);
        JdkVersion version = new JdkVersion();
        version.setIdentifier("temurin-21");

        File archive = new File("dummy.tar.gz");
        Path temp = Path.of("/tmp/extracted");
        InstallationRecord record = new InstallationRecord("temurin-21", Path.of("/install"), Path.of("/install/java"));
        ExtractedArtifact extractedArtifact = mock(ExtractedArtifact.class);

        when(catalog.findByIdentifier(platform, version.getIdentifier()))
            .thenReturn(Optional.of(version));
        when(releaseService.findJdkRelease(version))
            .thenReturn(Optional.empty());
        when(downloadService.download(any(), any()))
            .thenReturn(archive);
        when(layout.tempDirectory(version))
            .thenReturn(temp);
        when(extractor.unarchive(archive, temp))
            .thenReturn(extractedArtifact);
        when(installer.install(version, extractedArtifact))
            .thenReturn(record);

        // When
        service.install(platform, version.getIdentifier());

        // Then
        verify(publisher)
            .publish(new JdkInstallationStarted(version));
        verify(publisher)
            .publish(new JdkInstallationCompleted(version));

        verify(extractedArtifact).delete();
    }

    @Test
    void publishesEventsOnInstallFailure() throws IOException, InterruptedException {
        // Given
        Platform platform = new Platform(OperatingSystem.LINUX, ProcessorArchitecture.AARCH64);
        JdkVersion version = new JdkVersion();
        version.setIdentifier("temurin-21");

        File archive = new File("dummy.tar.gz");
        Path temp = Path.of("/tmp/extracted");
        ExtractedArtifact extractedArtifact = mock(ExtractedArtifact.class);
        RuntimeException installError = new RuntimeException("install error");

        when(catalog.findByIdentifier(platform, version.getIdentifier()))
            .thenReturn(Optional.of(version));
        when(releaseService.findJdkRelease(version))
            .thenReturn(Optional.empty());
        when(downloadService.download(any(), any()))
            .thenReturn(archive);
        when(layout.tempDirectory(version))
            .thenReturn(temp);
        when(extractor.unarchive(archive, temp))
            .thenReturn(extractedArtifact);
        when(installer.install(version, extractedArtifact))
            .thenThrow(installError);

        // When / Then
        Assertions.assertThatThrownBy(() -> service.install(platform, version.getIdentifier()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to install: temurin-21")
            .hasCause(installError);

        verify(publisher)
            .publish(new JdkInstallationStarted(version));
        verify(publisher)
            .publish(argThat(event ->
                event instanceof JdkInstallationFailed(JdkVersion v, Exception exception)
                    && v.equals(version)
                    && exception == installError));

        verify(extractedArtifact).delete();
    }
}
