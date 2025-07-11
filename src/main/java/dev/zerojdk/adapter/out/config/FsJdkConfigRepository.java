package dev.zerojdk.adapter.out.config;

import dev.zerojdk.domain.model.context.LayoutContext;
import dev.zerojdk.domain.port.out.layout.BaseLayout;
import dev.zerojdk.domain.port.out.config.JdkConfigRepository;
import dev.zerojdk.infrastructure.config.PropertiesConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

@RequiredArgsConstructor
public class FsJdkConfigRepository implements JdkConfigRepository {
    private final BaseLayout baseLayout;

    @Override
    public String readVersion(LayoutContext layoutContext) {
        return readVersionFromFile(baseLayout.configFile(layoutContext));
    }

    @Override
    public void update(String version, LayoutContext layoutContext) {
        writeVersion(baseLayout.configFile(layoutContext), version);
    }

    @Override
    public void create(String version, LayoutContext layoutContext) {
        baseLayout.ensureBaseDirectory(layoutContext);
        Path configFile = baseLayout.configFile(layoutContext);

        if (Files.exists(configFile)) {
            throw new ConfigFileAlreadyExistsException();
        }

        writeVersion(configFile, version);
    }

    private String readVersionFromFile(Path configFile) {
        try {
            PropertiesConfiguration configuration = PropertiesConfiguration.from(configFile);
            return configuration.getString("version");
        } catch (NoSuchFileException e) {
            throw new ConfigFileNotFoundException(e);
        } catch (IOException e) {
            // TODO: Improve
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    private void writeVersion(Path configFile, String version) {
        PropertiesConfiguration configuration = new PropertiesConfiguration();
        configuration.addProperty("version", version);

        configuration.save(configFile);
    }
}
