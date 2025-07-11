package dev.zerojdk.adapter.out.release;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.model.release.InstallationRecord;
import dev.zerojdk.domain.port.out.layout.JdkReleaseLayout;
import dev.zerojdk.domain.port.out.release.JavaHomeDetector;
import dev.zerojdk.domain.port.out.release.JdkRegistrationRepository;
import dev.zerojdk.domain.service.unarchiving.ExtractedArtifact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FsJdkInstallerTest {
    @TempDir
    private Path tempDir;

    @Mock
    private JdkReleaseLayout releaseLayout;
    @Mock
    private JdkRegistrationRepository registrationRepository;
    @Mock
    private JavaHomeDetector javaHomeDetector;

    @InjectMocks
    private FsJdkInstaller installer;

    @Test
    void installShouldMoveDirectoryAndRegister() throws Exception {
        // Given
        Path extracted = tempDir.resolve("extracted");
        Path bin = Files.createDirectories(extracted.resolve("bin"));
        Path javaBin = bin.resolve("java");
        Path releaseDir = Files.createDirectories(tempDir.resolve("releases"));
        Path targetDir = releaseDir.resolve("temurin-21");

        Files.createFile(javaBin);
        Files.setPosixFilePermissions(javaBin, PosixFilePermissions.fromString("rwxr-xr-x"));

        when(releaseLayout.ensureReleaseDirectory())
            .thenReturn(releaseDir);
        when(javaHomeDetector.detect(targetDir))
            .thenReturn(Optional.of(targetDir));

        JdkVersion version = new JdkVersion();
        version.setIdentifier("temurin-21");

        InstallationRecord expectedRecord = new InstallationRecord(
            "temurin-21", targetDir, extracted);

        when(registrationRepository.register(any()))
            .thenReturn(expectedRecord);

        // When
        InstallationRecord actual = installer.install(version, new ExtractedArtifact(extracted, null));

        // Then
        assertThat(actual.identifier())
            .isEqualTo("temurin-21");
        assertThat(actual.installRoot())
            .isEqualTo(targetDir);
        assertThat(actual.javaHome())
            .isEqualTo(extracted);

        verify(registrationRepository).register(any());
        assertThat(Files.exists(targetDir)).isTrue();
    }

    @Test
    void installShouldThrowIfJavaBinaryNotFound() throws Exception {
        // Given
        Path extracted = tempDir.resolve("bad-jdk");
        Files.createDirectories(extracted); // no java binary inside

        when(releaseLayout.ensureReleaseDirectory()).thenReturn(tempDir);

        JdkVersion version = new JdkVersion();
        version.setIdentifier("bad-jdk");

        // When / Then
        assertThatThrownBy(() -> installer.install(version, new ExtractedArtifact(extracted, null)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Extracted JDK home does not exist or is invalid");
    }
}
