package dev.zerojdk.adapter.out.release;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class FsJavaHomeDetectorTest {
    private final FsJavaHomeDetector detector = new FsJavaHomeDetector();

    @TempDir
    private Path tempDir;

    @Test
    void shouldDetectJavaHomeWithSingleBin(@TempDir Path tempDir) throws Exception {
        // Given
        Path javaHome = tempDir.resolve("jdk");
        Path binDir = javaHome.resolve("bin");

        Files.createDirectories(binDir);
        createExecutable(binDir.resolve("java"));

        Files.writeString(javaHome.resolve("release"), "JAVA_VERSION=\"1.8.0\"");

        // When
        Optional<Path> actual = detector.detect(tempDir);

        // Then
        assertThat(actual)
            .hasValue(javaHome);
    }

    @Test
    void shouldPreferJavaHomeWithReleaseFile() throws Exception {
        // Given
        Path javaHome = tempDir.resolve("jdk");
        Path javaHomeBin = javaHome.resolve("bin").resolve("java");
        Files.createDirectories(javaHomeBin.getParent());
        createExecutable(javaHomeBin);

        Path jreHome = javaHome.resolve("jre");
        Path jreHomeBin = jreHome.resolve("bin").resolve("java");
        Files.createDirectories(jreHomeBin.getParent());
        createExecutable(jreHomeBin);

        Files.writeString(javaHome.resolve("release"), "JAVA_VERSION=\"1.8.0\"");

        // When
        Optional<Path> actual = detector.detect(tempDir);

        // Then
        assertThat(actual)
            .hasValue(javaHome);
    }

    @Test
    void shouldFallbackToAnyJavaHomeIfNoReleaseFile() throws Exception {
        // Given
        Path someJavaHome = tempDir.resolve("jdk").resolve("jre");
        Path javaExecutable = someJavaHome.resolve("bin").resolve("java");
        Files.createDirectories(javaExecutable.getParent());
        createExecutable(javaExecutable);

        // When
        Optional<Path> actual = detector.detect(tempDir);

        // Then
        assertThat(actual).contains(someJavaHome);
    }

    @Test
    void shouldReturnEmptyIfNoJavaFound() {
        // When
        Optional<Path> actual = detector.detect(tempDir);

        // Then
        assertThat(actual).isEmpty();
    }

    private void createExecutable(Path path) throws Exception {
        Files.setPosixFilePermissions(
            Files.createFile(path),
            PosixFilePermissions.fromString("rwxr-xr-x"));
    }
}
