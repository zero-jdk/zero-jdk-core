package dev.zerojdk.domain.service.release;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.model.OperatingSystem;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.model.ProcessorArchitecture;
import dev.zerojdk.domain.model.release.InstallationRecord;
import dev.zerojdk.domain.model.release.JdkRelease;
import dev.zerojdk.domain.port.out.release.JdkRegistrationRepository;
import dev.zerojdk.domain.service.catalog.CatalogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JdkReleaseServiceTest {
    @Mock
    private CatalogService catalogService;
    @Mock
    private JdkRegistrationRepository repository;
    @InjectMocks
    private JdkReleaseService service;

    @Test
    void findsJdkReleaseIfRegistered() {
        // Given
        JdkVersion jdkVersion = new JdkVersion();
        jdkVersion.setIdentifier("temurin-21");
        Path root = Path.of("/path/to/temurin-21");
        Path home = root.resolve("home");

        when(repository.find("temurin-21"))
            .thenReturn(Optional.of(new InstallationRecord("temurin-21", root, home)));

        // When
        Optional<JdkRelease> actual = service.findJdkRelease(jdkVersion);

        // Then
        assertThat(actual)
            .hasValueSatisfying(release -> assertThat(release.jdkVersion()).isEqualTo(jdkVersion))
            .hasValueSatisfying(release -> assertThat(release.javaHome()).isEqualTo(home));
    }

    @Test
    void returnsEmptyIfNotRegistered() {
        // Given
        JdkVersion jdkVersion = new JdkVersion();
        jdkVersion.setIdentifier("unknown");

        when(repository.find("unknown"))
            .thenReturn(Optional.empty());

        // When / Then
        assertThat(service.findJdkRelease(jdkVersion))
            .isEmpty();
    }

    @Test
    void findsInstalledReleasesForPlatform() {
        // Given
        Platform platform = new Platform(OperatingSystem.LINUX, ProcessorArchitecture.AARCH64);
        JdkVersion jdkVersion = new JdkVersion();
        jdkVersion.setIdentifier("temurin-21");

        Path root = Path.of("/path/to/temurin-21");
        Path home = root.resolve("home");

        when(repository.findAll())
            .thenReturn(List.of(new InstallationRecord("temurin-21", root, home)));
        when(catalogService.findByIdentifier(platform, "temurin-21"))
            .thenReturn(Optional.of(jdkVersion));

        // WHen
        List<JdkRelease> results = service.findInstalledJdkReleases(platform);

        // Then
        assertThat(results)
            .extracting(JdkRelease::jdkVersion)
            .extracting(JdkVersion::getIdentifier)
            .containsExactly("temurin-21");
    }
}
