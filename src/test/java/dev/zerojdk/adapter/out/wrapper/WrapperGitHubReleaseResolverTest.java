package dev.zerojdk.adapter.out.wrapper;

import dev.zerojdk.domain.model.OperatingSystem;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.model.ProcessorArchitecture;
import dev.zerojdk.domain.port.out.wrapper.WrapperArtifactNameResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WrapperGitHubReleaseResolverTest {
    @Mock
    private WrapperArtifactNameResolver nameResolver;

    private WrapperGitHubReleaseResolver releaseResolver;

    @BeforeEach
    void setup() {
        releaseResolver = new WrapperGitHubReleaseResolver("https://www.foo.bar/v%s/%s", nameResolver);
    }

    @Test
    void resolvesCorrectUrl() {
        // Given
        when(nameResolver.resolve(any(), any()))
            .thenReturn("whatever-file-name.tar.gz");

        // When
        String actual = releaseResolver.resolveUrl(
            new Platform(OperatingSystem.LINUX, ProcessorArchitecture.AARCH64),
            "1.2.3");

        // Then
        assertThat(actual)
            .isEqualTo("https://www.foo.bar/v1.2.3/whatever-file-name.tar.gz");
    }
}
