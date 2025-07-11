package dev.zerojdk.domain.service.catalog.storage;

import dev.zerojdk.domain.model.catalog.Catalog;
import dev.zerojdk.domain.port.out.catalog.CatalogProviderService;
import dev.zerojdk.domain.model.catalog.storage.CatalogStorage;
import dev.zerojdk.domain.port.out.catalog.CatalogStorageMetadataRepository;
import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@RequiredArgsConstructor
public class CatalogStorageService {
    private final CatalogProviderService catalogProviderService;
    private final CatalogStorageMetadataRepository metadataRepository;

    public CatalogStorage updateCatalog() {
        Catalog catalog = catalogProviderService.provideLatest();
        return storeCatalog(catalog);
    }

    public CatalogStorage updateCatalogIfNewer() {
        return metadataRepository.getCurrentVersion()
            .map(CatalogStorage::version)
            .map(this::updateCatalogIfNewer)
            .orElseGet(this::updateCatalog);
    }

    public Optional<CatalogStorage> findCatalogStorage() {
        return metadataRepository.getCurrentVersion();
    }

    private CatalogStorage updateCatalogIfNewer(String currentVersion) {
        Catalog catalog = catalogProviderService.provideLatestIfNewer(currentVersion);
        return storeCatalog(catalog);
    }

    private CatalogStorage storeCatalog(Catalog catalog) {
        CatalogStorage metadata = metadataRepository.setCurrentVersion(catalog.version());
        move(catalog.location(), metadata.location());
        return metadata;
    }

    private void move(Path source, Path target) {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Failed to move catalog file", e);
        }
    }
}
