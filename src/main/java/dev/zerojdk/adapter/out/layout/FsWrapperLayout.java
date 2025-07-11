package dev.zerojdk.adapter.out.layout;

import dev.zerojdk.domain.model.context.LayoutContext;
import dev.zerojdk.domain.port.out.layout.BaseLayout;
import dev.zerojdk.domain.port.out.layout.UnmanagedDirectoryException;
import dev.zerojdk.domain.port.out.layout.WrapperLayout;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class FsWrapperLayout implements WrapperLayout {
    private static final String WRAPPER_DIRNAME = "wrapper";

    private final BaseLayout baseLayout;

    @SneakyThrows
    @Override
    public Path ensureWrapperDirectory(LayoutContext layoutContext) {
        return Files.createDirectories(wrapperDirectory(layoutContext));
    }

    @Override
    public Path binaryPath(LayoutContext layoutContext) {
        return wrapperDirectory(layoutContext)
            .resolve("zjdk");
    }

    @Override
    public Path configPath(LayoutContext layoutContext) {
        return wrapperDirectory(layoutContext)
            .resolve("zjdk-wrapper.properties");
    }

    @Override
    public Path scriptPath(LayoutContext layoutContext) {
        return baseLayout.discoverProjectRoot(layoutContext)
            .orElseThrow(() -> new UnmanagedDirectoryException(layoutContext))
            .resolve("zjdkw");
    }

    private Path wrapperDirectory(LayoutContext layoutContext) {
        return baseLayout.baseDirectory(layoutContext)
            .resolve(WRAPPER_DIRNAME);
    }
}
