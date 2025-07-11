package dev.zerojdk.domain.port.out.config;

import dev.zerojdk.domain.model.context.LayoutContext;

public interface JdkConfigRepository {
    String readVersion(LayoutContext layoutContext);
    void update(String version, LayoutContext layoutContext);
    void create(String version, LayoutContext layoutContext);
}
