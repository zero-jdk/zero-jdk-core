package dev.zerojdk.adapter.out.event;

import dev.zerojdk.domain.port.out.event.DomainEvent;
import dev.zerojdk.domain.port.out.event.DomainEventObserver;
import dev.zerojdk.domain.port.out.event.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ScopedEventObserver implements Observer {
    private final DomainEventObserver domainEventObserver;

    private final List<Observer> observers = new ArrayList<>();

    public ScopedEventObserver(DomainEventObserver domainEventObserver) {
        this.domainEventObserver = domainEventObserver;
    }

    public <T extends DomainEvent> void register(Class<T> type, Consumer<T> consumer) {
        observers.add(domainEventObserver.register(type, consumer));
    }

    @Override
    public void close() {
        observers.forEach(Observer::close);
    }
}
