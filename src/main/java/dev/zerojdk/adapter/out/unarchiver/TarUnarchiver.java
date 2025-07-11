package dev.zerojdk.adapter.out.unarchiver;

import dev.zerojdk.domain.port.out.unarchiving.Unarchiver;
import dev.zerojdk.domain.port.out.unarchiving.compression.Decompression;
import dev.zerojdk.domain.service.unarchiving.ExtractedArtifact;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static dev.zerojdk.adapter.out.unarchiver.UnarchiveUtils.createLink;

@RequiredArgsConstructor
public class TarUnarchiver implements Unarchiver {
    private final Path archive;
    @Getter
    private final Decompression decompression;

    @Override
    @SneakyThrows
    public ExtractedArtifact extract(Path destination) {
        executeInStream(stream -> extract(stream, destination))
            .forEach(Runnable::run);

        return new ExtractedArtifact(destination, new RecursiveCleanupPolicy());
    }

    private <T> T executeInStream(Function<TarArchiveInputStream, T> action) throws IOException {
        try (TarArchiveInputStream tarArchiveStream = new TarArchiveInputStream(decompression.decompress(
            new BufferedInputStream(new FileInputStream(archive.toFile()))))) {

            return action.apply(tarArchiveStream);
        }
    }

    @SneakyThrows
    private List<Runnable> extract(TarArchiveInputStream tar, Path destination) {
        List<Runnable> deferredSymlinks = new ArrayList<>();

        TarArchiveEntry entry;

        while ((entry = tar.getNextEntry()) != null) {
            if (entry.getName() == null) {
                continue;
            }

            Path target = destination.resolve(entry.getName()).normalize();

            if (!target.startsWith(destination)) {
                throw new IOException("Entry tries to escape target dir: " + target);
            }

            if (entry.isDirectory()) {
                Files.createDirectories(target);
                PosixPermissions.setPosixFilePermissions(target, entry.getMode());
            } else if (entry.isSymbolicLink() || entry.isLink()) {
                deferredSymlinks.add(createLink(target, entry.isSymbolicLink(), entry.getLinkName(), destination));
            } else {
                UnarchiveUtils.extractFile(tar, entry.getMode(), target);
            }
        }

        return deferredSymlinks;
    }
}
