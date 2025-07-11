package dev.zerojdk.adapter.out.unarchiver.decompression;

import dev.zerojdk.domain.port.out.unarchiving.compression.Decompression;

import java.io.InputStream;

public class NoDecompression implements Decompression {
    @Override
    public InputStream decompress(InputStream in) {
        return in;
    }
}
