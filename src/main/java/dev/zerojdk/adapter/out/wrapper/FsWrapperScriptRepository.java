package dev.zerojdk.adapter.out.wrapper;

import dev.zerojdk.domain.model.context.LayoutContext;
import dev.zerojdk.domain.port.out.layout.WrapperLayout;
import dev.zerojdk.domain.port.out.wrapper.WrapperScriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;

@RequiredArgsConstructor
public class FsWrapperScriptRepository implements WrapperScriptRepository {
    private final WrapperLayout wrapperLayout;

    @SneakyThrows
    @Override
    public void save(LayoutContext layoutContext, String content) {
        Path scriptPath = wrapperLayout.scriptPath(layoutContext);

        Files.writeString(scriptPath, content, StandardCharsets.UTF_8);

        // Make it executable
        var perms = new HashSet<>(Files.getPosixFilePermissions(scriptPath));
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(scriptPath, perms);
    }
}
