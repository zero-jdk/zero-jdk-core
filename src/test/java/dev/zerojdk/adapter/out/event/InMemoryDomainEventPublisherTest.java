package dev.zerojdk.adapter.out.event;

import dev.zerojdk.domain.port.out.event.DomainEvent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class InMemoryDomainEventPublisherTest {
    private final InMemoryDomainEventPublisher publisher = new InMemoryDomainEventPublisher();

    @Test
    void publishInvokesAllListenersForSameEventType() {
        // Given
        AtomicInteger count = new AtomicInteger(0);

        publisher.register(TestEvent.class, a -> count.incrementAndGet());
        publisher.register(TestEvent.class, a -> count.incrementAndGet());

        // When
        publisher.publish(new TestEvent());

        // Then
        assertThat(count.get())
            .isEqualTo(2);
    }

    @Test
    void publishDoesNothingIfNoListenersRegistered() {
        // When / Then
        assertDoesNotThrow(() -> publisher.publish(new TestEvent()));
    }

    // Simple test event
    static class TestEvent implements DomainEvent {}
}
