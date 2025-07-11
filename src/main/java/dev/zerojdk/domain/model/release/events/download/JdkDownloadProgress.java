package dev.zerojdk.domain.model.release.events.download;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.event.DomainEvent;

public record JdkDownloadProgress(JdkVersion version, long bytesRead, long totalBytes) implements DomainEvent { }
