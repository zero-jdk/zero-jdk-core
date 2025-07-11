package dev.zerojdk.adapter.out.event;

import dev.zerojdk.domain.port.out.event.DomainEvent;
import dev.zerojdk.domain.port.out.event.DomainEventObserver;
import dev.zerojdk.domain.port.out.event.DomainEventPublisher;
import dev.zerojdk.domain.port.out.event.Observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class InMemoryDomainEventPublisher implements DomainEventPublisher, DomainEventObserver {
    private final Map<Class<?>, List<Consumer<DomainEvent>>> listeners = new HashMap<>();

    public <T extends DomainEvent> Observer register(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, a -> new ArrayList<>())
            .add((Consumer<DomainEvent>) listener);

        return () -> {
            synchronized (InMemoryDomainEventPublisher.this) {
                List<Consumer<DomainEvent>> consumers = listeners.get(eventType);

                if (consumers != null) {
                    consumers.remove(listener);
                }
            }
        };
    }

    @Override
    public void publish(DomainEvent event) {
        Class<?> eventClass = event.getClass();

        listeners.getOrDefault(eventClass, List.of())
            .forEach(listener -> listener.accept(event));
    }
}
