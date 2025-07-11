package dev.zerojdk.adapter.out.config;

import dev.zerojdk.domain.model.context.LayoutContext;
import dev.zerojdk.domain.port.out.layout.BaseLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static dev.zerojdk.domain.model.context.LayoutContexts.local;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FsJdkConfigRepositoryTest {
    @Mock
    private BaseLayout baseLayout;

    @InjectMocks
    private FsJdkConfigRepository repository;

    @TempDir
    private File tempDir;

    private Path configFile;

    @BeforeEach
    void setup() {
        configFile = tempDir.toPath().resolve("config.properties");
    }

    @Test
    void readVersionReturnsCorrectValue() throws Exception {
        // Given
        LayoutContext layoutContext = local(tempDir.toPath());

        storeVersion("21.0.2");

        when(baseLayout.configFile(layoutContext))
            .thenReturn(configFile);

        // When
        String actual = repository.readVersion(layoutContext);

        // Then
        assertThat(actual)
            .isEqualTo("21.0.2");
    }

    @Test
    void updateWritesVersionToFile() throws Exception {
        // Given
        LayoutContext layoutContext = local(tempDir.toPath());

        when(baseLayout.configFile(layoutContext))
            .thenReturn(configFile);

        // When
        repository.update("17.0.10", layoutContext);

        // Then
        Properties properties = loadProperties();

        assertThat(properties.getProperty("version"))
            .isEqualTo("17.0.10");
    }

    @Test
    void createFailsIfConfigFileAlreadyExists() throws Exception {
        // Given
        LayoutContext layoutContext = local(tempDir.toPath());

        storeVersion("20.0.0");

        when(baseLayout.configFile(layoutContext))
            .thenReturn(configFile);

        // When / Then
        assertThatThrownBy(() -> repository.create("21.0.1", layoutContext))
            .isInstanceOf(ConfigFileAlreadyExistsException.class);
    }

    @Test
    void createWritesVersionIfFileDoesNotExist() throws Exception {
        // Given
        LayoutContext layoutContext = local(tempDir.toPath());

        when(baseLayout.configFile(layoutContext))
            .thenReturn(configFile);
        when(baseLayout.ensureBaseDirectory(layoutContext))
            .thenReturn(tempDir.toPath());

        // When
        repository.create("22.0.0", layoutContext);

        // Then
        Properties properties = loadProperties();

        assertThat(properties.getProperty("version"))
            .isEqualTo("22.0.0");
    }

    private void storeVersion(String version) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("version", version);

        try (var out = Files.newOutputStream(configFile)) {
            properties.store(out, null);
        }
    }

    private Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        try (var in = Files.newInputStream(configFile)) {
            properties.load(in);
        }

        return properties;
    }
}
