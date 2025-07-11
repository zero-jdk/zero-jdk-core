package dev.zerojdk.adapter.out.wrapper;

import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.port.out.wrapper.WrapperArtifactNameResolver;

public class WrapperGitHubArtifactNameResolver implements WrapperArtifactNameResolver {
    @Override
    public String resolve(Platform platform, String version) {
        String os = switch (platform.os()) {
            case LINUX -> "linux";
            case MACOS -> "macos";
            case WINDOWS -> "windows";
            case AIX -> "aix";
        };

        String arch = switch (platform.arch()) {
            case AARCH64 -> "arm64";
            case X64 -> "x64";
        };

        // zjdk-0.1.0-linux-arm64.tar.gz
        return String.format("zjdk-%s-%s-%s.tar.gz",
            version,
            os,
            arch);
    }
}
