package dev.zerojdk.adapter.out.catalog.storage;

import dev.zerojdk.domain.model.catalog.storage.CatalogStorage;
import dev.zerojdk.domain.port.out.catalog.CatalogStorageMetadataRepository;
import dev.zerojdk.domain.port.out.layout.CatalogStorageLayout;
import dev.zerojdk.infrastructure.config.PropertiesConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@RequiredArgsConstructor
public class FsCatalogStorageMetadataRepository implements CatalogStorageMetadataRepository {
    private final CatalogStorageLayout catalogStorageLayout;

    @SneakyThrows
    @Override
    public Optional<CatalogStorage> getCurrentVersion() {
        Path metadataFile = catalogStorageLayout.metadataFile();

        if (!Files.exists(metadataFile)) {
            return Optional.empty();
        }

        PropertiesConfiguration configuration = PropertiesConfiguration.from(metadataFile);

        return Optional.of(new CatalogStorage(
            configuration.getString("version"),
            catalogStorageLayout.catalogFile()));
    }

    @SneakyThrows
    @Override
    public CatalogStorage setCurrentVersion(String version) {
        catalogStorageLayout.ensureCatalogStorageDirectory();

        PropertiesConfiguration configuration = new PropertiesConfiguration();
        configuration.addProperty("version", version);

        configuration.save(catalogStorageLayout.metadataFile());

        return new CatalogStorage(version, catalogStorageLayout.catalogFile());
    }
}