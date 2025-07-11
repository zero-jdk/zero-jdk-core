package dev.zerojdk.domain.model.release.events.installation;

import dev.zerojdk.domain.port.out.event.DomainEvent;

public record JdkInstallationFailed(dev.zerojdk.domain.model.JdkVersion version, Exception exception) implements DomainEvent { }
