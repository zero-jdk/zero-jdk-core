package dev.zerojdk.domain.service.install;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.model.release.InstallationRecord;
import dev.zerojdk.domain.model.release.JdkRelease;
import dev.zerojdk.domain.model.release.events.download.JdkDownloadCompleted;
import dev.zerojdk.domain.model.release.events.download.JdkDownloadFailed;
import dev.zerojdk.domain.model.release.events.download.JdkDownloadProgress;
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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.Optional;

@RequiredArgsConstructor
public class JdkInstallService {
    @NonNull
    private final DomainEventPublisher publisher;
    @NonNull
    private final JdkReleaseLayout jdkReleaseLayout;
    @NonNull
    private final DownloadService downloadService;
    @NonNull
    private final ArchiveExtractionService archiveExtractionService;
    @NonNull
    private final CatalogService catalogService;
    @NonNull
    private final JdkInstaller jdkInstaller;
    @NonNull
    private final JdkReleaseService jdkReleaseService;

    public InstallationRecord install(Platform platform, String identifier) {
        JdkVersion jdkVersion = catalogService.findByIdentifier(platform, identifier)
            .orElseThrow(() -> new UnsupportedIdentifierException(identifier));

        Optional<JdkRelease> jdkRelease = jdkReleaseService.findJdkRelease(jdkVersion);

        if (jdkRelease.isPresent()) {
            return jdkRelease.map(release ->
                new InstallationRecord(identifier, release.installRoot(), release.javaHome()))
                .get();
        }

        File archive = download(jdkVersion);

        ExtractedArtifact extractedArtifact = archiveExtractionService.unarchive(
            archive,
            jdkReleaseLayout.tempDirectory(jdkVersion));

        try {
            return install(jdkVersion, extractedArtifact);
        } finally {
            extractedArtifact.delete();
        }
    }

    private File download(JdkVersion version) {
        publisher.publish(new JdkDownloadStarted(version));

        try {
            File archive = downloadService.download(
                version.getIndirectDownloadUri(),
                (read, total) -> publisher.publish(new JdkDownloadProgress(version, read, total)));

            publisher.publish(new JdkDownloadCompleted(version));

            return archive;
        } catch (Exception e) {
            publisher.publish(new JdkDownloadFailed(version, e));
            throw new JdkDownloadFailedException(version);
        }
    }

    private InstallationRecord install(JdkVersion version, ExtractedArtifact extractedDirectory) {
        publisher.publish(new JdkInstallationStarted(version));

        try {
            InstallationRecord installationRecord = jdkInstaller.install(
                version,
                extractedDirectory);

            publisher.publish(new JdkInstallationCompleted(version));

            return installationRecord;
        } catch (Exception e) {
            publisher.publish(new JdkInstallationFailed(version, e));
            // TODO: proper exception
            throw new RuntimeException("Failed to install: " + version.getIdentifier(), e);
        }
    }
}
