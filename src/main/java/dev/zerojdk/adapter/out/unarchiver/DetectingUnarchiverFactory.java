package dev.zerojdk.adapter.out.unarchiver;

import dev.zerojdk.domain.port.out.unarchiving.Unarchiver;
import dev.zerojdk.domain.port.out.unarchiving.UnarchiverFactory;
import dev.zerojdk.adapter.out.unarchiver.decompression.GzipDecompression;
import dev.zerojdk.adapter.out.unarchiver.decompression.NoDecompression;

import java.io.File;

public class DetectingUnarchiverFactory implements UnarchiverFactory {
    @Override
    public Unarchiver create(File archive) {
        String name = archive.getName().toLowerCase();

        return switch (name) {
            case String s when s.endsWith(".tar.gz") || name.endsWith(".tgz") ->
                new TarUnarchiver(archive.toPath(), new GzipDecompression());
            case String s when s.endsWith("tar") ->
                new TarUnarchiver(archive.toPath(), new NoDecompression());
            case String s when s.endsWith("gz") ->
                new GzipUnarchiver(archive.toPath());
            case String s when s.endsWith("zip") ->
                new ZipUnarchiver(archive.toPath());
            default -> throw new UnsupportedArchiveException("Unsupported archive: " + name);
        };
    }
}
