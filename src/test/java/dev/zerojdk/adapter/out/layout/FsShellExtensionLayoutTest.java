package dev.zerojdk.adapter.out.layout;

import dev.zerojdk.domain.model.context.GlobalLayoutContext;
import dev.zerojdk.domain.port.out.layout.BaseLayout;
import dev.zerojdk.domain.port.out.layout.ShellExtensionLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, SystemStubsExtension.class })
class FsShellExtensionLayoutTest {
    @TempDir
    private Path tempDir;

    @Mock
    private BaseLayout baseLayout;

    private ShellExtensionLayout layout;

    @BeforeEach
    void setup() {
        // Given
        when(baseLayout.ensureBaseDirectory(new GlobalLayoutContext()))
            .thenReturn(tempDir);

        layout = new FsShellExtensionLayout(baseLayout);
    }

    @Test
    void ensureDirectoryExistsShouldCreateExtensionsDirectoryInsideBaseDirectory() {
        // When
        Path actual = layout.ensureDirectoryExists();

        // Then
        assertThat(actual)
            .exists()
            .isDirectory()
            .isEqualTo(tempDir.resolve("extensions"));
    }
}
