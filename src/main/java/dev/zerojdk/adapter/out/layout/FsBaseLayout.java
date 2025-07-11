package dev.zerojdk.adapter.out.layout;

import dev.zerojdk.domain.model.context.GlobalLayoutContext;
import dev.zerojdk.domain.model.context.LayoutContexts;
import dev.zerojdk.domain.port.out.layout.UnmanagedDirectoryException;
import dev.zerojdk.domain.model.context.LayoutContext;
import dev.zerojdk.domain.port.out.layout.BaseLayout;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class FsBaseLayout implements BaseLayout {
    private static final String ZJDK_DIRNAME = ".zjdk";

    /**
     * Locate the directory that contains a *zjdk* configuration.
     * Starts in the current working directory and walks up the filesystem hierarchy, ignoring the user's home directory
     * and stops before the filesystem root, returning the first ancestor that contains {@code .zjdk}.
     *
     * @return an {@link Optional} with the path of the directory that holds the
     *         zjdk configuration, or {@link Optional#empty()} if none is found
     */
    @Override
    public Optional<Path> discoverProjectRoot(LayoutContext layoutContext) {
        Path global = LayoutContexts.global().path().toAbsolutePath();
        Path path = layoutContext.path();

        while (path != null) {
            if (!path.equals(global)) {
                Path candidate = path.resolve(ZJDK_DIRNAME);
                if (Files.exists(candidate)) {
                    return Optional.of(path);
                }
            }

            path = path.getParent();
        }

        return Optional.empty();
    }

    @Override
    public Path configFile(LayoutContext layoutContext) {
        return baseDirectory(layoutContext).resolve("config.properties");
    }

    @Override
    public Path baseDirectory(LayoutContext layoutContext) throws UnmanagedDirectoryException {
        if (layoutContext instanceof GlobalLayoutContext) {
            return layoutContext.path().resolve(ZJDK_DIRNAME);
        }

        return discoverProjectRoot(layoutContext)
            .orElseThrow(() -> new UnmanagedDirectoryException(layoutContext))
            .resolve(ZJDK_DIRNAME);
    }

    @Override
    public Path ensureBaseDirectory(LayoutContext layoutContext) {
        return createBaseDirectory(layoutContext.path());
    }

    @SneakyThrows
    private Path createBaseDirectory(Path root) {
        return Files.createDirectories(
            root.resolve(ZJDK_DIRNAME));
    }
}
