package dev.zerojdk.domain.port.out.layout;

import java.nio.file.Path;

public interface ShellExtensionLayout {
    Path ensureDirectoryExists();
}
