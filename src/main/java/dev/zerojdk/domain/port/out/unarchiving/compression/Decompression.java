package dev.zerojdk.domain.port.out.unarchiving.compression;

import java.io.InputStream;

public interface Decompression {
    InputStream decompress(InputStream in);
}
