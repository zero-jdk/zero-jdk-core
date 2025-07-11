package dev.zerojdk.adapter.out.layout;

import dev.zerojdk.domain.port.out.layout.UnmanagedDirectoryException;
import dev.zerojdk.domain.model.context.GlobalLayoutContext;
import dev.zerojdk.domain.model.context.LocalLayoutContext;
import dev.zerojdk.domain.model.context.LayoutContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

@ExtendWith({MockitoExtension.class, SystemStubsExtension.class})
class FsBaseLayoutTest {
    private final FsBaseLayout baseLayout = new FsBaseLayout();

    @SystemStub
    private SystemProperties systemProperties;

    @TempDir
    private Path tempDir;

    private Path homeDir;
    private Path projectDir;

    @BeforeEach
    void setup() throws Exception {
        homeDir = Files.createDirectories(tempDir.resolve("home"));
        systemProperties.set("user.home", homeDir.toString());

        projectDir = Files.createDirectories(homeDir.resolve("project"));
    }

    @Test
    void discoverProjectRootReturnsEmptyIfNoZjdkDirectoryAnywhere() {
        // When / Then
        assertThat(baseLayout.discoverProjectRoot(new LocalLayoutContext(projectDir)))
            .isEmpty();
    }

    @Test
    void discoverProjectRootReturnsEmptyIfOnlyInHomeDirectory() throws Exception {
        // Given
        Files.createDirectories(homeDir.resolve(".zjdk"));

        // When / Then
        assertThat(baseLayout.discoverProjectRoot(new LocalLayoutContext(projectDir)))
            .isEmpty();
    }

    @Test
    void discoverProjectRootReturnsCurrentDirIfZjdkExistsThere() throws Exception {
        // Given
        Files.createDirectories(
            projectDir.resolve(".zjdk"));

        // When / Then
        assertThat(baseLayout.discoverProjectRoot(new LocalLayoutContext(projectDir)))
            .contains(projectDir);
    }

    @Test
    void discoverProjectRootReturnsParentDirIfZjdkExistsThere() throws Exception {
        // Given
        Path subdir = Files.createDirectories(
            projectDir.resolve("subdir"));
        Files.createDirectories(projectDir.resolve( ".zjdk"));

        // When / Then
        assertThat(baseLayout.discoverProjectRoot(new LocalLayoutContext(subdir)))
            .contains(projectDir);
    }

    @Test
    void configFileShouldReturnPathBasedOnLocalContext() throws IOException {
        // Given
        Files.createDirectories(
            projectDir.resolve(".zjdk"));

        LayoutContext layoutContext = new LocalLayoutContext(projectDir);

        // Then
        Path actual = baseLayout.configFile(layoutContext);

        // When
        assertThat(actual.toString())
            .startsWith(projectDir.toString())
            .endsWith(".zjdk/config.properties");
    }

    @Test
    void configFileShouldReturnPathBasedOnGlobalContext() {
        // Given
        LayoutContext layoutContext = new GlobalLayoutContext();

        // Then
        Path actual = baseLayout.configFile(layoutContext);

        // When
        assertThat(actual.toString())
            .startsWith(homeDir.toString())
            .endsWith(".zjdk/config.properties");
    }

    @Test
    void configFileShouldThrowIfLocalContextNotManaged() throws IOException {
        // Given
        Path unrelated = Files.createDirectories(
            tempDir.resolve("unmanaged-project"));

        // When / Then
        assertThatThrownBy(() -> baseLayout.configFile(new LocalLayoutContext(unrelated)))
            .isInstanceOf(UnmanagedDirectoryException.class);
    }

    @Test
    void ensureBaseDirectoryShouldCreateGlobal() {
        // Given
        LayoutContext globalContext = new GlobalLayoutContext();
        Path baseDirectory = homeDir.resolve(".zjdk");
        assertThat(baseDirectory).doesNotExist();

        // Then
        baseLayout.ensureBaseDirectory(globalContext);

        // When
        assertThat(baseDirectory)
            .exists()
            .isDirectory();
    }

    @Test
    void ensureBaseDirectoryShouldCreateLocal() {
        // Given
        LayoutContext localContext = new LocalLayoutContext(projectDir);
        Path baseDirectory = projectDir.resolve(".zjdk");
        assertThat(baseDirectory).doesNotExist();

        // Then
        baseLayout.ensureBaseDirectory(localContext);

        // When
        assertThat(baseDirectory)
            .exists()
            .isDirectory();
    }
}
