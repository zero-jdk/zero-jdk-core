package dev.zerojdk.adapter.out.layout;

import dev.zerojdk.domain.model.context.GlobalLayoutContext;
import dev.zerojdk.domain.port.out.layout.BaseLayout;
import dev.zerojdk.domain.model.context.LayoutContexts;
import dev.zerojdk.domain.port.out.layout.CatalogStorageLayout;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class FsCatalogStorageLayout implements CatalogStorageLayout {
    private final BaseLayout baseLayout;

    @Override
    public Path metadataFile() {
        return catalogStorageDirectory()
            .resolve("catalog.properties");
    }

    @Override
    public Path catalogFile() {
        return catalogStorageDirectory()
            .resolve("catalog.json");
    }

    @Override
    public Path ensureCatalogStorageDirectory() throws IOException {
        return Files.createDirectories(baseLayout.ensureBaseDirectory(new GlobalLayoutContext())
            .resolve("catalog"));
    }

    private Path catalogStorageDirectory() {
        return baseLayout.baseDirectory(new GlobalLayoutContext())
            .resolve("catalog");
    }
}
