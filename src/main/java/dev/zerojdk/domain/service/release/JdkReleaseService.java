package dev.zerojdk.domain.service.release;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.model.release.JdkRelease;
import dev.zerojdk.domain.port.out.release.JdkRegistrationRepository;
import dev.zerojdk.domain.service.catalog.CatalogService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class JdkReleaseService {
    private final CatalogService catalogService;
    private final JdkRegistrationRepository repository;

    public Optional<JdkRelease> findJdkRelease(JdkVersion jdkVersion) {
        return repository.find(jdkVersion.getIdentifier())
            .map(installationRecord ->
                new JdkRelease(jdkVersion, installationRecord.installRoot(), installationRecord.javaHome()));
    }

    public Optional<JdkRelease> findJdkRelease(Platform platform, String identifier) {
        return catalogService.findByIdentifier(platform, identifier)
            .flatMap(this::findJdkRelease);
    }

    public List<JdkRelease> findInstalledJdkReleases(Platform platform) {
        return repository.findAll().stream()
                .flatMap(installationRecord ->
                    catalogService.findByIdentifier(platform, installationRecord.identifier())
                        .map(jdkVersion -> new JdkRelease(jdkVersion, installationRecord.installRoot(), installationRecord.javaHome()))
                        .stream()).toList();
    }
}
