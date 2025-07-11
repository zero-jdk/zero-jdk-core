package dev.zerojdk.adapter.out.unarchiver;

import dev.zerojdk.domain.service.unarchiving.CleanupPolicy;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RecursiveCleanupPolicy implements CleanupPolicy {
    @Override
    public void cleanup(Path path) {
        try {
            if (Files.isDirectory(path)) {
                FileUtils.deleteDirectory(path.toFile());
            } else {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to cleanup: " + path, e);
        }
    }
}
