package dev.zerojdk.domain.model.context;

import java.nio.file.Path;

public sealed interface LayoutContext permits GlobalLayoutContext, LocalLayoutContext {
    Path path();
}

