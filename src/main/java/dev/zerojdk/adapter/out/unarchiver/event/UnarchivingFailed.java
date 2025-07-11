package dev.zerojdk.adapter.out.unarchiver.event;

import dev.zerojdk.domain.port.out.event.DomainEvent;

public record UnarchivingFailed(Exception exception) implements DomainEvent { }
