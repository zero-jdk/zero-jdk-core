package dev.zerojdk.domain.service.config;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.model.OperatingSystem;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.model.ProcessorArchitecture;
import dev.zerojdk.domain.model.context.LayoutContext;
import dev.zerojdk.domain.model.context.LocalLayoutContext;
import dev.zerojdk.domain.port.out.config.JdkConfigRepository;
import dev.zerojdk.domain.service.catalog.CatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JdkConfigServiceTest {
    @Mock
    private JdkConfigRepository jdkConfigRepository;

    @Mock
    private CatalogService catalogService;

    @InjectMocks
    private JdkConfigService service;

    private Platform platform;

    @BeforeEach
    void setUp() {
        platform = new Platform(OperatingSystem.LINUX, ProcessorArchitecture.AARCH64);
    }

    @Test
    void getActiveVersionReturnsVersionFromRepository() {
        // Given
        LayoutContext layoutContext = new LocalLayoutContext(Path.of("."));

        when(jdkConfigRepository.readVersion(layoutContext))
            .thenReturn("21.0.2");

        // When
        String actual = service.getActiveVersion(layoutContext);

        // Then
        assertThat(actual)
            .isEqualTo("21.0.2");
    }

    @Test
    void updateConfigurationThrowsIfIdentifierNotSupported() {
        // Given
        LayoutContext layoutContext = new LocalLayoutContext(Path.of("."));

        when(catalogService.findByIdentifier(platform, "invalid"))
            .thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.createConfiguration(platform, "invalid", layoutContext))
            .isInstanceOfSatisfying(UnsupportedIdentifierException.class,
                ex -> assertThat(ex.getIdentifier()).isEqualTo("invalid"));
    }

    @Test
    void updateConfigurationUpdatesRepositoryIfIdentifierExists() {
        // Given
        LayoutContext layoutContext = new LocalLayoutContext(Path.of("."));

        when(catalogService.findByIdentifier(platform, "21.0.2"))
            .thenReturn(Optional.of(new JdkVersion()));

        // When
        service.updateConfiguration(platform, "21.0.2", layoutContext);

        // Then
        verify(jdkConfigRepository)
            .update("21.0.2", layoutContext);
    }

    @Test
    void createConfigurationWithExplicitIdentifierValidatesAndCreates() {
        // Given
        LayoutContext layoutContext = new LocalLayoutContext(Path.of("."));

        when(catalogService.findByIdentifier(platform, "21.0.2"))
            .thenReturn(Optional.of(new JdkVersion()));

        // When
        service.createConfiguration(platform, "21.0.2", layoutContext);

        // Then
        verify(jdkConfigRepository)
            .create("21.0.2", layoutContext);
    }

    @Test
    void createConfigurationWithoutVersionUsesDefaultLtsFromTemurin() {
        // Given
        LayoutContext layoutContext = new LocalLayoutContext(Path.of("."));

        JdkVersion jdkVersion = new JdkVersion();
        jdkVersion.setIdentifier("temurin-21");
        jdkVersion.setSupport(JdkVersion.Support.LTS);

        when(catalogService.findLatestByDistribution(platform, "Temurin"))
            .thenReturn(List.of(jdkVersion));
        when(catalogService.findByIdentifier(platform, "temurin-21"))
            .thenReturn(Optional.of(jdkVersion));

        // When
        service.createConfiguration(platform, null, layoutContext);

        // Then
        verify(jdkConfigRepository)
            .create("temurin-21", layoutContext);
    }

    @Test
    void createConfigurationWithoutVersionThrowsIfNoLtsVersionFound() {
        // Given
        LayoutContext layoutContext = new LocalLayoutContext(Path.of("."));

        when(catalogService.findLatestByDistribution(platform, "Temurin"))
            .thenReturn(List.of());

        // Then
        assertThatThrownBy(() -> service.createConfiguration(platform, null, layoutContext))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("There was an issue resolving the default version");
    }

    @Test
    void createConfigurationThrowsIfDefaultIdentifierInvalid() {
        // Given
        LayoutContext layoutContext = new LocalLayoutContext(Path.of("."));

        JdkVersion jdkVersion = new JdkVersion();
        jdkVersion.setIdentifier("temurin-21");
        jdkVersion.setSupport(JdkVersion.Support.LTS);

        when(catalogService.findLatestByDistribution(platform, "Temurin"))
            .thenReturn(List.of(jdkVersion));
        when(catalogService.findByIdentifier(platform, "temurin-21"))
            .thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.createConfiguration(platform, null, layoutContext))
            .isInstanceOfSatisfying(UnsupportedIdentifierException.class,
                ex -> assertThat(ex.getIdentifier()).isEqualTo("temurin-21"));
    }
}
