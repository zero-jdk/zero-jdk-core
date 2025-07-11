package dev.zerojdk.adapter.out.release;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.model.release.InstallationRecord;
import dev.zerojdk.domain.port.out.release.JavaHomeDetector;
import dev.zerojdk.domain.port.out.release.JdkInstaller;
import dev.zerojdk.domain.port.out.release.JdkRegistrationRepository;
import dev.zerojdk.domain.port.out.layout.JdkReleaseLayout;
import dev.zerojdk.domain.service.unarchiving.ExtractedArtifact;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class FsJdkInstaller implements JdkInstaller {
    private final JdkReleaseLayout jdkReleaseLayout;
    private final JdkRegistrationRepository repository;
    private final JavaHomeDetector javaHomeDetector;

    @SneakyThrows
    @Override
    public InstallationRecord install(JdkVersion version, ExtractedArtifact artifact) {
        Path target = jdkReleaseLayout.ensureReleaseDirectory()
            .resolve(version.getIdentifier());

        Path actualRoot = findActualRoot(artifact.getPath());

        Files.move(actualRoot, target, StandardCopyOption.ATOMIC_MOVE);

        return javaHomeDetector.detect(target)
            .map(javaHome ->
                repository.register(new InstallationRecord(version.getIdentifier(), target, javaHome)))
            .orElseThrow(() -> new IllegalStateException("Extracted JDK home does not exist or is invalid"));
    }

    private Path findActualRoot(Path path) throws IOException {
        Path current = path.toAbsolutePath();

        while (Files.isDirectory(current)) {
            try (Stream<Path> entries = Files.list(current)) {
                Path[] contents = entries.toArray(Path[]::new);

                if (contents.length != 1 || !Files.isDirectory(contents[0])) {
                    return current;
                }

                current = contents[0];
            }
        }

        // TODO: proper exception - this however should never happen
        throw new RuntimeException("Could not find actual JDK root at " + current);
    }
}
