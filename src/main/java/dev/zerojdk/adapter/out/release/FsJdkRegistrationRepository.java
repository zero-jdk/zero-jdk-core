package dev.zerojdk.adapter.out.release;

import dev.zerojdk.domain.model.release.InstallationRecord;
import dev.zerojdk.domain.port.out.release.JdkRegistrationRepository;
import dev.zerojdk.domain.port.out.layout.JdkReleaseLayout;
import dev.zerojdk.infrastructure.config.PropertiesConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class FsJdkRegistrationRepository implements JdkRegistrationRepository {
    private final JdkReleaseLayout jdkReleaseLayout;

    @SneakyThrows
    @Override
    public InstallationRecord register(InstallationRecord installationRecord) {
        Path info = installationRecord.installRoot()
            .resolve(".info");

        PropertiesConfiguration configuration = new PropertiesConfiguration();
        configuration.addProperty("home", installationRecord.javaHome().toAbsolutePath());

        configuration.save(info);

        return installationRecord;
    }

    @SneakyThrows
    @Override
    public Optional<InstallationRecord> find(String identifier) {
        Path release = jdkReleaseLayout.ensureReleaseDirectory()
            .resolve(identifier);

        return map(release);
    }

    @Override
    @SneakyThrows
    public List<InstallationRecord> findAll() {
        try (Stream<Path> files =  Files.list(jdkReleaseLayout.releaseDirectory())) {
            return files.flatMap(release -> map(release).stream())
                .toList();
        }
    }

    @SneakyThrows
    private Optional<InstallationRecord> map(Path release) {
        Path info = release
            .resolve(".info");

        if (!Files.exists(info)) {
            return Optional.empty();
        }

        String identifier = release.getFileName().toString();

        PropertiesConfiguration configuration = PropertiesConfiguration.from(info);

        return Optional.ofNullable(configuration.getString("home"))
            .map(home -> new InstallationRecord(identifier, release.toAbsolutePath(), Path.of(home)));
    }
}
