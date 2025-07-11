package dev.zerojdk.adapter.out.event;

import dev.zerojdk.domain.port.out.event.DomainEvent;
import dev.zerojdk.domain.port.out.event.DomainEventObserver;
import dev.zerojdk.domain.port.out.event.Observer;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.mockito.Mockito.*;

class ScopedEventObserverTest {
    @Test
    void registersObserversAndClosesThemOnClose() {
        // Given
        DomainEventObserver domainEventObserver = mock(DomainEventObserver.class);
        Observer observer1 = mock(Observer.class);
        Observer observer2 = mock(Observer.class);

        Consumer<DummyEvent> consumer1 = mock(Consumer.class);
        Consumer<DummyEvent> consumer2 = mock(Consumer.class);

        when(domainEventObserver.register(DummyEvent.class, consumer1)).thenReturn(observer1);
        when(domainEventObserver.register(DummyEvent.class, consumer2)).thenReturn(observer2);


        // When
        ScopedEventObserver scoped = new ScopedEventObserver(domainEventObserver);

        scoped.register(DummyEvent.class, consumer1);
        scoped.register(DummyEvent.class, consumer2);

        scoped.close();

        // Then
        verify(observer1).close();
        verify(observer2).close();
    }

    static class DummyEvent implements DomainEvent {}
}
