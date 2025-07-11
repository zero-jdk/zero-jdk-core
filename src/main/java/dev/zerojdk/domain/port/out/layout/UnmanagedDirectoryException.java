package dev.zerojdk.domain.port.out.layout;

import dev.zerojdk.domain.model.context.LayoutContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UnmanagedDirectoryException extends RuntimeException {
    private final LayoutContext layoutContext;
}
