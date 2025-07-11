package dev.zerojdk.domain.service.wrapper;

import dev.zerojdk.domain.model.context.LayoutContext;
import dev.zerojdk.domain.port.out.layout.WrapperLayout;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RequiredArgsConstructor
public class BinaryInstaller {
    private final WrapperLayout wrapperLayout;

    @SneakyThrows
    public void install(Path executable, LayoutContext layoutContext) {
        Files.copy(executable, wrapperLayout.binaryPath(layoutContext), StandardCopyOption.REPLACE_EXISTING);
    }
}
