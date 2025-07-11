package dev.zerojdk.adapter.out.layout;

import dev.zerojdk.domain.model.context.LocalLayoutContext;
import dev.zerojdk.domain.port.out.layout.BaseLayout;
import dev.zerojdk.domain.port.out.layout.UnmanagedDirectoryException;
import dev.zerojdk.domain.port.out.layout.WrapperLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({ MockitoExtension.class, SystemStubsExtension.class })
class FsWrapperLayoutTest {
    @TempDir
    private Path tempDir;

    @Mock
    private BaseLayout baseLayout;

    private WrapperLayout wrapperLayout;

    @BeforeEach
    void setup() {
        // Given
        wrapperLayout = new FsWrapperLayout(baseLayout);
    }

    @Test
    void ensureWrapperDirectoryShouldCreateWrapperDirectory() {
        // Given
        LocalLayoutContext context = new LocalLayoutContext(tempDir);
        when(baseLayout.baseDirectory(context))
            .thenReturn(tempDir);

        // When
        Path actual = wrapperLayout.ensureWrapperDirectory(context);

        // Then
        assertThat(actual)
            .exists()
            .isDirectory()
            .isEqualTo(tempDir.resolve("wrapper"));
    }

    @Test
    void binaryPathShouldResolveZjdkBinaryInsideWrapperDirectory() {
        // Given
        LocalLayoutContext context = new LocalLayoutContext(tempDir);
        when(baseLayout.baseDirectory(context))
            .thenReturn(tempDir);

        // When
        Path actual = wrapperLayout.binaryPath(context);

        // Then
        assertThat(actual)
            .isEqualTo(tempDir.resolve("wrapper/zjdk"));
    }

    @Test
    void configPathShouldResolvePropertiesFileInsideWrapperDirectory() {
        // Given
        LocalLayoutContext context = new LocalLayoutContext(tempDir);
        when(baseLayout.baseDirectory(context))
            .thenReturn(tempDir);

        // When
        Path actual = wrapperLayout.configPath(context);

        // Then
        assertThat(actual)
            .isEqualTo(tempDir.resolve("wrapper/zjdk-wrapper.properties"));
    }

    @Test
    void scriptPathShouldResolveToProjectRootZjdkwScript() {
        // Given
        LocalLayoutContext context = new LocalLayoutContext(tempDir);
        when(baseLayout.discoverProjectRoot(context))
            .thenReturn(Optional.of(tempDir));

        // When
        Path actual = wrapperLayout.scriptPath(context);

        // Then
        assertThat(actual)
            .isEqualTo(tempDir.resolve("zjdkw"));
    }

    @Test
    void scriptPathShouldThrowIfProjectRootNotFound() {
        // Given
        LocalLayoutContext context = new LocalLayoutContext(tempDir);
        when(baseLayout.discoverProjectRoot(context))
            .thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> wrapperLayout.scriptPath(context))
            .isInstanceOf(UnmanagedDirectoryException.class);
    }
}
