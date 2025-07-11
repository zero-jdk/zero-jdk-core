package dev.zerojdk.domain.model.release.events.download;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.event.DomainEvent;

public record JdkDownloadStarted(JdkVersion version) implements DomainEvent {

}
