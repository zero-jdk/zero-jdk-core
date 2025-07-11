package dev.zerojdk.adapter.out.wrapper;

import dev.zerojdk.domain.model.context.LocalLayoutContext;
import dev.zerojdk.domain.model.wrapper.WrapperConfig;
import dev.zerojdk.domain.port.out.layout.WrapperLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FsWrapperConfigRepositoryTest {
    @TempDir
    private Path tempDir;

    @Mock
    private WrapperLayout layout;

    @InjectMocks
    private FsWrapperConfigRepository repository;

    @Test
    void readShouldReturnEmptyIfFileDoesNotExist() {
        // Given
        LocalLayoutContext context = new LocalLayoutContext(tempDir);

        when(layout.configPath(context))
            .thenReturn(tempDir.resolve("foo-bar.properties"));

        // When
        Optional<WrapperConfig> actual = repository.read(context);

        // Then
        assertThat(actual)
            .isEmpty();
    }

    @Test
    void writeShouldStoreUrlInPropertiesFile() {
        // Given
        Path configPath = tempDir.resolve("baz.properties");

        LocalLayoutContext context = new LocalLayoutContext(tempDir);

        when(layout.configPath(context))
            .thenReturn(configPath);
        when(layout.ensureWrapperDirectory(context))
            .thenReturn(tempDir);

        // When
        WrapperConfig actual = repository.write(
            context,
            new WrapperConfig("https://example.com"));

        // Then
        assertThat(configPath)
            .exists()
            .hasContent("url=https://example.com");

        assertThat(actual.url())
            .isEqualTo("https://example.com");
    }

    @Test
    void readShouldReturnStoredWrapperConfig() throws IOException {
        // Given
        Path configPath = tempDir.resolve("bar.properties");

        LocalLayoutContext context = new LocalLayoutContext(tempDir);
        when(layout.configPath(context))
            .thenReturn(configPath);

        Files.writeString(configPath, "url=https://stored-url.com");

        // When
        Optional<WrapperConfig> result = repository.read(context);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().url()).isEqualTo("https://stored-url.com");
    }
}
