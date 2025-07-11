package dev.zerojdk.domain.port.out.event;

public interface Observer extends AutoCloseable {
    @Override
    void close();
}
