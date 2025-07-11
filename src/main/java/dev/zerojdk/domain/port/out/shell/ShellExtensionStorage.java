package dev.zerojdk.domain.port.out.shell;

import dev.zerojdk.domain.service.shell.ShellScript;

import java.nio.file.Path;

public interface ShellExtensionStorage {
    Path write(String name, String content);
}
