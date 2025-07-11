package dev.zerojdk.domain.service.unarchiving;

import lombok.Data;

import java.nio.file.Path;

@Data
public class ExtractedArtifact {
    private final Path path;
    private final CleanupPolicy cleanupPolicy;

    public void delete() {
        if (cleanupPolicy != null) {
            cleanupPolicy.cleanup(path);
        }
    }
}
