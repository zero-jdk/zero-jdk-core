package dev.zerojdk.adapter.out.shell;

import dev.zerojdk.domain.port.out.layout.ShellExtensionLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FsShellExtensionStorageTest {
    @TempDir
    private Path tempDir;

    @Mock
    private ShellExtensionLayout layout;

    @InjectMocks
    private FsShellExtensionStorage storage;

    @Test
    void writeShouldCreateFileWithGivenContent() throws Exception {
        // Given
        String name = "zjdk.plugin.zsh";
        String content = "echo hello";

        when(layout.ensureDirectoryExists())
            .thenReturn(tempDir);

        // When
        Path actual = storage.write(name, content);

        // Then
        assertThat(actual)
            .exists()
            .isRegularFile()
            .hasFileName(name);

        assertThat(Files.readString(actual))
            .isEqualTo(content);
    }
}
