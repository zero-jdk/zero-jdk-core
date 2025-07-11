package dev.zerojdk.domain.port.out.wrapper;

import dev.zerojdk.adapter.out.wrapper.WrapperGitHubArtifactNameResolver;
import dev.zerojdk.domain.model.OperatingSystem;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.model.ProcessorArchitecture;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class WrapperGitHubArtifactNameResolverTest {
    private final WrapperGitHubArtifactNameResolver resolver = new WrapperGitHubArtifactNameResolver();

    @ParameterizedTest
    @CsvSource({
        "LINUX, AARCH64, 0.2.0, zjdk-0.2.0-linux-arm64.tar.gz",
        "MACOS, X64, 1.0.0, zjdk-1.0.0-macos-x64.tar.gz",
        "WINDOWS, X64, 2.1.1, zjdk-2.1.1-windows-x64.tar.gz"
    })
    void resolvesCorrectArtifactName(OperatingSystem os, ProcessorArchitecture arch, String version, String expectedName) {
        // Given
        Platform platform = new Platform(os, arch);

        // When
        String result = resolver.resolve(platform, version);

        // Then
        assertThat(result).isEqualTo(expectedName);
    }
}
