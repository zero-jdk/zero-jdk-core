package dev.zerojdk.adapter.out.layout;

import dev.zerojdk.domain.port.out.layout.BaseLayout;
import dev.zerojdk.domain.model.context.LayoutContexts;
import dev.zerojdk.domain.port.out.layout.ShellExtensionLayout;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class FsShellExtensionLayout implements ShellExtensionLayout {
    private final BaseLayout baseLayout;

    @SneakyThrows
    public Path ensureDirectoryExists() {
        return Files.createDirectories(baseLayout.ensureBaseDirectory(LayoutContexts.global())
            .resolve("extensions"));
    }
}
