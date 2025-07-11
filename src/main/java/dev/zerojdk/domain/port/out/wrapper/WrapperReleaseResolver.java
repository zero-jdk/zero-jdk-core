package dev.zerojdk.domain.port.out.wrapper;

import dev.zerojdk.domain.model.Platform;

public interface WrapperReleaseResolver {
    String resolveUrl(Platform platform, String version);
}
