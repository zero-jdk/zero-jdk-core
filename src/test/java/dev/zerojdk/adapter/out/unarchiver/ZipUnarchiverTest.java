package dev.zerojdk.adapter.out.unarchiver;

import dev.zerojdk.domain.service.unarchiving.ExtractedArtifact;
import org.apache.commons.compress.archivers.zip.UnixStat;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;

class ZipUnarchiverTest {
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
    void shouldExtractFilesFromZipArchive() throws IOException {
        // Given
        ArchiveEntry file1 = ArchiveEntry.file("file1.txt", "Content of file 1");
        ArchiveEntry file2 = ArchiveEntry.file("file2.txt", "Content of file 2");

        // When
        ExtractedArtifact actual = new ZipUnarchiver(
            createZipArchive(file1, file2)).extract(tempDir);

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
    void shouldExtractSymbolicLinks() throws IOException {
        // Given
        Path archivePath = createZipArchive(
            ArchiveEntry.file("target.txt", "Lorem ipsum"),
            ArchiveEntry.symlink("link-to-target", "target.txt"));

        // When
        ExtractedArtifact actual = new ZipUnarchiver(archivePath).extract(tempDir);

        // Then
        Path target = actual.getPath().resolve("target.txt");
        Path link = actual.getPath().resolve("link-to-target");

        assertThat(target)
            .exists()
            .hasContent("Lorem ipsum");
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

    private Path createZipArchive(ArchiveEntry... entries) throws IOException {
        Path archivePath = tempDir.resolve("archive.zip");

        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(
            new BufferedOutputStream(Files.newOutputStream(archivePath)))) {

            for (ArchiveEntry archiveEntry : entries) {
                ZipArchiveEntry entry = new ZipArchiveEntry(archiveEntry.name());

                switch (archiveEntry.kind()) {
                    case DIRECTORY -> // drwxr-xr-x
                        entry.setUnixMode(040755);
                    case FILE -> {
                        // -rw-r--r--
                        entry.setUnixMode(0100644);
                        entry.setSize(archiveEntry.content().getBytes().length);
                    }
                    case SYMLINK -> {
                        entry.setUnixMode(UnixStat.LINK_FLAG | UnixStat.DEFAULT_LINK_PERM);
                        entry.setSize(archiveEntry.content().getBytes().length);
                        entry.setMethod(ZipArchiveEntry.DEFLATED);
                    }
                }

                zos.putArchiveEntry(entry);

                if (archiveEntry.kind() != ArchiveEntry.Kind.DIRECTORY) {
                    zos.write(archiveEntry.content().getBytes());
                }

                zos.closeArchiveEntry();
            }
        }

        return archivePath;
    }
}
