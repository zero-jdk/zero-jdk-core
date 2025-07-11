package dev.zerojdk.domain.model.catalog.storage.event;

import dev.zerojdk.domain.port.out.event.DomainEvent;

public record CatalogDownloadProgress(String version, long bytesRead, long totalBytes) implements DomainEvent { }
