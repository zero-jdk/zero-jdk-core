package dev.zerojdk.domain.model.release.events.download;

import dev.zerojdk.domain.port.out.event.DomainEvent;

public record JdkDownloadFailed(dev.zerojdk.domain.model.JdkVersion version, Exception cause) implements DomainEvent { }
