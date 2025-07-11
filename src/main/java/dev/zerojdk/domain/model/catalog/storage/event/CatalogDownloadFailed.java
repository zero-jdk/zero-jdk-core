package dev.zerojdk.domain.model.catalog.storage.event;

import dev.zerojdk.domain.port.out.event.DomainEvent;

public record CatalogDownloadFailed(Exception exception) implements DomainEvent { }
