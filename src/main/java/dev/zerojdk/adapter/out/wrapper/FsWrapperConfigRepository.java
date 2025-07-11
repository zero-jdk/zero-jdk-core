package dev.zerojdk.adapter.out.wrapper;

import dev.zerojdk.domain.model.context.LayoutContext;
import dev.zerojdk.domain.model.wrapper.WrapperConfig;
import dev.zerojdk.domain.port.out.wrapper.WrapperConfigRepository;
import dev.zerojdk.domain.port.out.layout.WrapperLayout;
import dev.zerojdk.infrastructure.config.PropertiesConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@RequiredArgsConstructor
public class FsWrapperConfigRepository implements WrapperConfigRepository {
    private final WrapperLayout wrapperLayout;

    @SneakyThrows
    @Override
    public Optional<WrapperConfig> read(LayoutContext layoutContext) {
        Path path = wrapperLayout.configPath(layoutContext);

        if (!Files.exists(path)) {
            return Optional.empty();
        }

        PropertiesConfiguration properties = PropertiesConfiguration.from(path);
        properties.getString("url");

        return Optional.of(new WrapperConfig(properties.getString("url")));
    }

    @SneakyThrows
    @Override
    public WrapperConfig write(LayoutContext layoutContext, WrapperConfig wrapperConfig) {
        wrapperLayout.ensureWrapperDirectory(layoutContext);

        PropertiesConfiguration  properties = new PropertiesConfiguration();
        properties.addProperty("url", wrapperConfig.url());
        properties.save(wrapperLayout.configPath(layoutContext));

        return wrapperConfig;
    }
}
