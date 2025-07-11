package dev.zerojdk.adapter.out.wrapper;

import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.port.out.wrapper.WrapperArtifactNameResolver;
import dev.zerojdk.domain.port.out.wrapper.WrapperReleaseResolver;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WrapperGitHubReleaseResolver implements WrapperReleaseResolver {
    private final String urlTemplate;
    private final WrapperArtifactNameResolver wrapperArtifactNameResolver;

    @Override
    public String resolveUrl(Platform platform, String version) {
        String name = wrapperArtifactNameResolver.resolve(platform, version);
        return String.format(urlTemplate, version, name);
    }
}
