package dev.zerojdk.domain.service.shell;

import dev.zerojdk.domain.port.out.shell.ShellExtensionStorage;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

@RequiredArgsConstructor
public class ShellExtensionWriter {
    private final ShellExtensionStorage shellExtensionStorage;

    public Path write(ShellScript script) {
        return shellExtensionStorage.write(script.getName(), script.getContent());
    }
}
