package dev.zerojdk.adapter.out.shell;

import dev.zerojdk.domain.port.out.layout.ShellExtensionLayout;
import dev.zerojdk.domain.port.out.shell.ShellExtensionStorage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@RequiredArgsConstructor
public class FsShellExtensionStorage implements ShellExtensionStorage {
    private final ShellExtensionLayout extensionLayout;

    @Override
    @SneakyThrows
    public Path write(String name, String content) {
        return Files.writeString(
            extensionLayout.ensureDirectoryExists()
                .resolve(name),
            content,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        );
    }
}
