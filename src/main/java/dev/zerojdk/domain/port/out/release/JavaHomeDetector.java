package dev.zerojdk.domain.port.out.release;

import java.nio.file.Path;
import java.util.Optional;

public interface JavaHomeDetector {
    Optional<Path> detect(Path root);
}
