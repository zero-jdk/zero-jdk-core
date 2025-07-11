package dev.zerojdk.adapter.out.unarchiver.decompression;

import dev.zerojdk.domain.port.out.unarchiving.compression.Decompression;
import lombok.SneakyThrows;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.InputStream;

public class GzipDecompression implements Decompression {
    @SneakyThrows
    @Override
    public InputStream decompress(InputStream in) {
        return new GzipCompressorInputStream(in);
    }
}
