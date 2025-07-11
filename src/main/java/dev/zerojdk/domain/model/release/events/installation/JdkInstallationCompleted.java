package dev.zerojdk.domain.model.release.events.installation;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.event.DomainEvent;

public record JdkInstallationCompleted(JdkVersion version) implements DomainEvent { }
