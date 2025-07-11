package dev.zerojdk.domain.model.context;

import java.nio.file.Path;

public record LocalLayoutContext(Path path) implements LayoutContext { }
