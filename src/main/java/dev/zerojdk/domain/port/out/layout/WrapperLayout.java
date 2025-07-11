package dev.zerojdk.domain.port.out.layout;

import dev.zerojdk.domain.model.context.LayoutContext;

import java.nio.file.Path;

public interface WrapperLayout {
    Path ensureWrapperDirectory(LayoutContext layoutContext);
    Path binaryPath(LayoutContext layoutContext);
    Path configPath(LayoutContext layoutContext);
    Path scriptPath(LayoutContext layoutContext);
}
