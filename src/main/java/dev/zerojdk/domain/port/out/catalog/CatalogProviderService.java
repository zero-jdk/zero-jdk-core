package dev.zerojdk.domain.port.out.catalog;

import dev.zerojdk.domain.model.catalog.Catalog;

public interface CatalogProviderService {
    Catalog provideLatest();
    Catalog provideLatestIfNewer(String currentVersion);
}