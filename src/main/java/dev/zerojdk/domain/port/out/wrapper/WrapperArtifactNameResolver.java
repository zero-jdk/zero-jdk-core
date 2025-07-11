package dev.zerojdk.domain.port.out.wrapper;

import dev.zerojdk.domain.model.Platform;

public interface WrapperArtifactNameResolver {
    String resolve(Platform platform, String version);
}
