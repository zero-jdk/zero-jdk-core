package dev.zerojdk.adapter.out.unarchiver;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class UnarchiveUtils {
    private UnarchiveUtils() { }

    public static void extractFile(InputStream in, int unixMode, Path target) throws IOException {
        Files.createDirectories(target.getParent());

        try (OutputStream out = Files.newOutputStream(target)) {
            byte[] buffer = new byte[8192];

            int length;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        }

        PosixPermissions.setPosixFilePermissions(target, unixMode);
    }

    public static Runnable createLink(Path target, boolean symbolic, String linkName, Path extractionRoot) {
        return () -> {
            try {
                Files.createDirectories(target.getParent());
                Files.deleteIfExists(target);

                if (symbolic) {
                    Files.createSymbolicLink(target, Paths.get(linkName));
                } else {
                    Files.createLink(target, extractionRoot.resolve(linkName).normalize());
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
}
