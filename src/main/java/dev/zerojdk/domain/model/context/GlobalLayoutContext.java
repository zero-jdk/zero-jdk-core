package dev.zerojdk.domain.model.context;

import lombok.Data;

import java.nio.file.Path;

@Data
public final class GlobalLayoutContext implements LayoutContext {
    @Override
    public Path path() {
        return Path.of(System.getProperty("user.home"));
    }
}
