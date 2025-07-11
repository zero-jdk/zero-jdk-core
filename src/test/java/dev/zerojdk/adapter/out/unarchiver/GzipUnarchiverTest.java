package dev.zerojdk.adapter.out.unarchiver;

import dev.zerojdk.domain.service.unarchiving.ExtractedArtifact;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class GzipUnarchiverTest {
    @TempDir
    private Path tempDir;

    @ParameterizedTest
    @CsvSource({
        "foo.gz, foo",
        "foo.text.gz, foo.text",
        ".foo.gz, .foo"
    })
    void shouldExtractGzFileCorrectly(Path archiveName, String extractedName) throws IOException {
        // Given
        String content = "This is a test string for GzipUnarchiver.";

        File archive = createGzipFile(
            tempDir.resolve(archiveName),
            content);

        GzipUnarchiver unarchiver = new GzipUnarchiver(archive.toPath());

        // When
        ExtractedArtifact actual = unarchiver.extract(tempDir);

        // Then
        assertThat(actual.getPath())
            .exists()
            .hasFileName(extractedName);

        assertThat(Files.readString(actual.getPath()))
            .isEqualTo(content);
    }

    private File createGzipFile(Path archivePath, String content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(archivePath.toFile());
             GZIPOutputStream gos = new GZIPOutputStream(fos)) {
            gos.write(content.getBytes());
        }

        return archivePath.toFile();
    }
}

