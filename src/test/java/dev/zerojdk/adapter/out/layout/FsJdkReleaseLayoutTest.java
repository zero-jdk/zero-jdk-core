package dev.zerojdk.adapter.out.layout;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.model.context.GlobalLayoutContext;
import dev.zerojdk.domain.port.out.layout.BaseLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith({ MockitoExtension.class, SystemStubsExtension.class })
class FsJdkReleaseLayoutTest {
    @TempDir
    private Path tempDir;

    @Mock
    private BaseLayout baseLayout;

    private FsJdkReleaseLayout layout;

    @BeforeEach
    void setup() {
        when(baseLayout.baseDirectory(new GlobalLayoutContext()))
            .thenReturn(tempDir);

        layout = new FsJdkReleaseLayout(baseLayout);
    }

    @Test
    void releaseDirectoryShouldBeInsideBaseLayout() {
        // When
        Path actual = layout.releaseDirectory();

        // Then
        assertThat(actual)
            .isEqualTo(tempDir.resolve("releases"));
    }

    @Test
    void ensureReleaseDirectoryShouldCreateDirectory() {
        // When
        Path actual = layout.ensureReleaseDirectory();

        // Then
        assertThat(actual)
            .exists()
            .isDirectory()
            .isEqualTo(tempDir.resolve("releases"));
    }

    @Test
    void tempDirectoryShouldResolvePartDirectoryForVersion() {
        // Given
        JdkVersion version = new JdkVersion();
        version.setIdentifier("temurin-21");

        // When
        Path actual = layout.tempDirectory(version);

        // Then
        assertThat(actual)
            .isEqualTo(tempDir.resolve("releases").resolve(version.getIdentifier() + ".part"));
    }
}
