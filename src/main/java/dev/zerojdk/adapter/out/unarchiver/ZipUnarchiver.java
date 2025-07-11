package dev.zerojdk.adapter.out.unarchiver;

import dev.zerojdk.domain.port.out.unarchiving.Unarchiver;
import dev.zerojdk.domain.service.unarchiving.ExtractedArtifact;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static dev.zerojdk.adapter.out.unarchiver.UnarchiveUtils.createLink;

@RequiredArgsConstructor
public class ZipUnarchiver implements Unarchiver  {
    private final Path archive;

    @SneakyThrows
    @Override
    public ExtractedArtifact extract(Path destination) {
        List<Runnable> deferredSymlinks = new ArrayList<>();

        try (ZipFile zf = ZipFile.builder().setPath(archive).get()) {
            Enumeration<ZipArchiveEntry> entries = zf.getEntries();

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();

                Path target = destination.resolve(entry.getName()).normalize();
                if (!target.startsWith(destination))
                    throw new IOException("Entry tries to escape target dir: " + target);

                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                    PosixPermissions.setPosixFilePermissions(target, entry.getUnixMode());
                }
                else if (entry.isUnixSymlink()) {
                    String linkTarget = new String(zf.getInputStream(entry).readAllBytes(),
                        StandardCharsets.UTF_8);

                    deferredSymlinks.add(createLink(target, true, linkTarget, destination));
                }
                else {
                    try (InputStream in = zf.getInputStream(entry)) {
                        UnarchiveUtils.extractFile(in, entry.getUnixMode(), target);
                    }
                }
            }
        }

        deferredSymlinks.forEach(Runnable::run);
        return new ExtractedArtifact(destination, new RecursiveCleanupPolicy());
    }
}
