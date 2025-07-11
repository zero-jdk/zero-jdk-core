package dev.zerojdk.domain.model.context;

import java.nio.file.Path;

public class LayoutContexts {
    public static LayoutContext global() {
        return new GlobalLayoutContext();
    }

    public static LayoutContext local(Path path) {
        return new LocalLayoutContext(path);
    }

    public static LayoutContext current() {
        return new LocalLayoutContext(Path.of("."));
    }

    public static boolean isGlobalContext(LayoutContext context) {
        return context instanceof LocalLayoutContext
            && context.path().toAbsolutePath().normalize().equals(global().path());
    }
}
