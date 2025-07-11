package dev.zerojdk.domain.port.out.layout;

import dev.zerojdk.domain.model.context.LayoutContext;

import java.nio.file.Path;
import java.util.Optional;

public interface BaseLayout {
    Optional<Path> discoverProjectRoot(LayoutContext layoutContext);

    Path configFile(LayoutContext layoutContext);
    Path baseDirectory(LayoutContext layoutContext) throws UnmanagedDirectoryException;
    Path ensureBaseDirectory(LayoutContext layoutContext);
}
