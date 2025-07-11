package dev.zerojdk.domain.service.unarchiving;

import java.nio.file.Path;

public interface CleanupPolicy {
    void cleanup(Path path);
}
