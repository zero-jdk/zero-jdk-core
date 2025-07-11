package dev.zerojdk.adapter.out.layout;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.layout.BaseLayout;
import dev.zerojdk.domain.model.context.LayoutContexts;
import dev.zerojdk.domain.port.out.layout.JdkReleaseLayout;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class FsJdkReleaseLayout implements JdkReleaseLayout {
    private final BaseLayout baseLayout;

    @Override
    public Path tempDirectory(JdkVersion version) {
        return ensureReleaseDirectory()
            .resolve(version.getIdentifier() + ".part");
    }

    @SneakyThrows
    @Override
    public Path ensureReleaseDirectory() {
        return Files.createDirectories(releaseDirectory());
    }

    @Override
    public Path releaseDirectory() {
        return baseLayout.baseDirectory(LayoutContexts.global())
            .resolve("releases");
    }
}
