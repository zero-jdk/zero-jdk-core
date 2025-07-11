package dev.zerojdk.adapter.out.release;

import dev.zerojdk.domain.model.release.InstallationRecord;
import dev.zerojdk.domain.port.out.layout.JdkReleaseLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FsJdkRegistrationRepositoryTest {
    @TempDir
    private Path tempDir;

    @Mock
    private JdkReleaseLayout releaseLayout;

    private FsJdkRegistrationRepository repository;

    @BeforeEach
    void setup() {
        repository = new FsJdkRegistrationRepository(releaseLayout);
    }

    @Test
    void registerShouldPersistMetadataFile() throws Exception {
        // Given
        Path installRoot = tempDir.resolve("temurin-21");
        Path javaHome = Files.createDirectories(installRoot.resolve("home"));

        InstallationRecord record = new InstallationRecord("temurin-21", installRoot, javaHome);

        // When
        InstallationRecord actual = repository.register(record);

        // Then
        assertThat(actual)
            .isEqualTo(record);

        assertThat(installRoot.resolve(".info"))
            .exists()
            .hasContent("home=" + javaHome.toAbsolutePath());
    }

    @Test
    void findShouldReturnInstallationRecordIfInfoFileExists() throws Exception {
        // Given
        String identifier = "temurin-21";
        Path releaseDir = tempDir.resolve(identifier);
        Path javaHome = Files.createDirectories(releaseDir.resolve("home"));

        Path info = releaseDir.resolve(".info");
        Files.writeString(info, "home=" + javaHome.toAbsolutePath());

        when(releaseLayout.ensureReleaseDirectory())
            .thenReturn(tempDir);

        // When
        Optional<InstallationRecord> result = repository.find(identifier);

        // Then
        assertThat(result)
            .hasValueSatisfying(record -> {
                assertThat(record.identifier())
                    .isEqualTo(identifier);
                assertThat(record.installRoot())
                    .isEqualTo(releaseDir.toAbsolutePath());
                assertThat(record.javaHome())
                    .isEqualTo(javaHome.toAbsolutePath());
            });
    }

    @Test
    void findShouldReturnEmptyIfInfoFileMissing() {
        // Given
        String identifier = "unknown";
        when(releaseLayout.ensureReleaseDirectory())
            .thenReturn(tempDir);

        // When / Then
        assertThat(repository.find(identifier))
            .isEmpty();
    }
}
