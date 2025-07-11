package dev.zerojdk.domain.port.out.wrapper;

import dev.zerojdk.domain.model.context.LayoutContext;
import dev.zerojdk.domain.model.wrapper.WrapperConfig;

import java.util.Optional;

public interface WrapperConfigRepository {
    Optional<WrapperConfig> read(LayoutContext layoutContext);
    WrapperConfig write(LayoutContext layoutContext, WrapperConfig url);
}
