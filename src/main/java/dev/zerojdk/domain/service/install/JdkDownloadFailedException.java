package dev.zerojdk.domain.service.install;

import dev.zerojdk.domain.model.JdkVersion;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class JdkDownloadFailedException extends RuntimeException {
    private final JdkVersion jdkVersion;
}
