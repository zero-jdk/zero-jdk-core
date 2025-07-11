package dev.zerojdk.domain.port.out.wrapper;

import dev.zerojdk.domain.model.context.LayoutContext;

public interface WrapperScriptRepository {
    void save(LayoutContext layoutContext, String content);
}
