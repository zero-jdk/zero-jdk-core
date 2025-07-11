package dev.zerojdk.domain.model.release.events.download;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.event.DomainEvent;

public record JdkDownloadCompleted(JdkVersion version) implements DomainEvent { }
