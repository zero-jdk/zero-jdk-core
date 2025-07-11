package dev.zerojdk.domain.port.out.unarchiving;

import dev.zerojdk.domain.service.unarchiving.ExtractedArtifact;

import java.nio.file.Path;

public interface Unarchiver {
    ExtractedArtifact extract(Path target);
}
