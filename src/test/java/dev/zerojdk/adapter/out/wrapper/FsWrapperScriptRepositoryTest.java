package dev.zerojdk.adapter.out.wrapper;

import dev.zerojdk.domain.model.context.LocalLayoutContext;
import dev.zerojdk.domain.port.out.layout.WrapperLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FsWrapperScriptRepositoryTest {
    @TempDir
    private Path tempDir;

    @Mock
    private WrapperLayout wrapperLayout;

    @InjectMocks
    private FsWrapperScriptRepository repository;

    @Test
    void saveShouldWriteScriptContentToCorrectPathAndMakeItExecutable() throws Exception {
        // Given
        LocalLayoutContext context = new LocalLayoutContext(tempDir);
        Path scriptPath = tempDir.resolve("foo");

        when(wrapperLayout.scriptPath(context))
            .thenReturn(scriptPath);

        String content = "#!/bin/bash\necho Hello";

        // When
        repository.save(context, content);

        // Then
        assertThat(scriptPath)
            .exists()
            .hasContent(content);

        assertThat(Files.getPosixFilePermissions(scriptPath))
            .contains(PosixFilePermission.OWNER_EXECUTE);
    }
}
