package dev.zerojdk.adapter.out.unarchiver;

import dev.zerojdk.adapter.out.unarchiver.decompression.GzipDecompression;
import dev.zerojdk.adapter.out.unarchiver.decompression.NoDecompression;
import dev.zerojdk.domain.service.unarchiving.ExtractedArtifact;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;

class TarUnarchiverTest {
    @TempDir
    private Path tempDir;

    private MockedStatic<PosixPermissions> mockedPosixPermissions;

    @BeforeEach
    void setupPosixPermissions() {
        mockedPosixPermissions = Mockito.mockStatic(PosixPermissions.class);
    }

    @AfterEach
    void resetPosixPermissions() {
        mockedPosixPermissions.close();
    }

    @Test
    void shouldExtractFromCompressedArchive() throws IOException {
        // Given
        ArchiveEntry file1 = ArchiveEntry.file("file1.txt", "Content of file 1");
        ArchiveEntry file2 = ArchiveEntry.file("file2.txt", "Content of file 2");

        // When
        ExtractedArtifact actual = new TarUnarchiver(
            createTarArchive(true, file1,  file2),
            new GzipDecompression()).extract(tempDir);

        // Then
        assertThat(actual.getPath())
            .isEqualTo(tempDir);
        assertThat(tempDir.resolve(file1.name()))
            .exists()
            .hasContent(file1.content());
        assertThat(tempDir.resolve(file2.name()))
            .exists()
            .hasContent(file2.content());

        mockedPosixPermissions.verify(() ->
            PosixPermissions.setPosixFilePermissions(any(Path.class), anyInt()), times(2));
    }

    @Test
    void shouldExtractFilesFromUncompressedArchive() throws IOException {
        // Given
        ArchiveEntry file1 = ArchiveEntry.file("file1.txt", "Content of file 1");

        // When
        ExtractedArtifact actual = new TarUnarchiver(
            createTarArchive(false, file1),
            new NoDecompression()).extract(tempDir);

        // Then
        assertThat(actual.getPath())
            .isEqualTo(tempDir);
        assertThat(tempDir.resolve(file1.name()))
            .exists()
            .hasContent(file1.content());

        mockedPosixPermissions.verify(() ->
            PosixPermissions.setPosixFilePermissions(any(Path.class), anyInt()), times(1));
    }

    @Test
    void shouldExtractSymbolicLink() throws IOException {
        // Given
        Path archive = createTarArchive(false,
            ArchiveEntry.file("target.txt", "consectetur adipiscing elit"),
            ArchiveEntry.symlink("sym-link", "target.txt"));

        // When
        ExtractedArtifact actual = new TarUnarchiver(archive, new NoDecompression()).extract(tempDir);

        // Then
        Path target = actual.getPath().resolve("target.txt");
        Path link = actual.getPath().resolve("sym-link");

        assertThat(target)
            .exists()
            .hasContent("consectetur adipiscing elit");
        assertThat(link)
            .exists()
            .isSymbolicLink();

        assertThat(Files.readSymbolicLink(link))
            .isEqualTo(target.getFileName());
        assertThat(link.toRealPath())
            .isEqualTo(target.toRealPath());

        mockedPosixPermissions.verify(
            () -> PosixPermissions.setPosixFilePermissions(any(Path.class), anyInt()), times(1));
    }

    @Test
    void shouldExtractHardLink() throws IOException {
        // Given
        Path archive = createTarArchive(false,
            ArchiveEntry.file("target.txt", "Lorem Ipsum"),
            ArchiveEntry.hardlink("hard-link", "target.txt"));

        // When
        ExtractedArtifact actual = new TarUnarchiver(archive, new NoDecompression()).extract(tempDir);

        // Then
        Path target = actual.getPath().resolve("target.txt");
        Path link = actual.getPath().resolve("hard-link");

        assertThat(target)
            .exists()
            .hasContent("Lorem Ipsum");
        assertThat(link)
            .exists()
            .isRegularFile();

        assertThat(Files.isSameFile(link, target))
            .isTrue();

        mockedPosixPermissions.verify(
            () -> PosixPermissions.setPosixFilePermissions(any(Path.class), anyInt()), times(1));
    }

    private Path createTarArchive(boolean gzipped, ArchiveEntry... entries) throws IOException {
        Path archivePath = tempDir.resolve("archive.xyz");

        OutputStream outputStream =
            new BufferedOutputStream(Files.newOutputStream(archivePath));

        if (gzipped) {
            outputStream = new GzipCompressorOutputStream(outputStream);
        }

        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(outputStream)) {
            for (ArchiveEntry archiveEntry : entries) {
                TarArchiveEntry entry = switch (archiveEntry.kind()) {
                    case DIRECTORY -> new TarArchiveEntry(archiveEntry.name());
                    case FILE -> {
                        TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(archiveEntry.name());
                        tarArchiveEntry.setSize(archiveEntry.content().getBytes().length);
                        yield tarArchiveEntry;
                    }
                    case SYMLINK -> {
                        TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(
                            archiveEntry.name(),
                            TarArchiveEntry.LF_SYMLINK);
                        tarArchiveEntry.setLinkName(archiveEntry.content());
                        yield tarArchiveEntry;
                    }
                    case HARDLINK   -> {
                        TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(
                            archiveEntry.name(),
                            TarArchiveEntry.LF_LINK);
                        tarArchiveEntry.setLinkName(archiveEntry.content());
                        yield tarArchiveEntry;
                    }
                };

                tos.putArchiveEntry(entry);

                if (archiveEntry.kind() == ArchiveEntry.Kind.FILE) {
                    tos.write(archiveEntry.content().getBytes());
                }

                tos.closeArchiveEntry();
            }
        }

        return archivePath;
    }
}