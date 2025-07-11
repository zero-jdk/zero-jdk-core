package dev.zerojdk.adapter.out.unarchiver;

import dev.zerojdk.domain.port.out.unarchiving.Unarchiver;
import dev.zerojdk.adapter.out.unarchiver.decompression.GzipDecompression;
import dev.zerojdk.domain.service.unarchiving.ExtractedArtifact;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RequiredArgsConstructor
public class GzipUnarchiver implements Unarchiver {
    private final Path archive;

    @SneakyThrows
    @Override
    public ExtractedArtifact extract(Path target) {
        try (InputStream inputStream = new GzipDecompression().decompress(
            new BufferedInputStream(
                new FileInputStream(archive.toFile())))) {

            Path targetFile = target.resolve(removeExtension(archive).getFileName());

            Files.copy(inputStream,
                targetFile,
                StandardCopyOption.REPLACE_EXISTING);

            return new ExtractedArtifact(targetFile, new RecursiveCleanupPolicy());
        }
    }

    private Path removeExtension(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');

        // dotIndex == 0 handles hidden files
        if (dotIndex == -1 || dotIndex == 0) {
            return path;
        }

        return path.resolveSibling(
            fileName.substring(0, dotIndex));
    }
}
