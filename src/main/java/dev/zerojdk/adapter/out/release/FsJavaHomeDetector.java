package dev.zerojdk.adapter.out.release;

import dev.zerojdk.domain.port.out.release.JavaHomeDetector;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FsJavaHomeDetector implements JavaHomeDetector {
    @SneakyThrows
    @Override
    public Optional<Path> detect(Path root) {
        try (Stream<Path> paths = Files.walk(root)) {
            List<Path> javaHomes = paths
                .filter(p -> p.getFileName().toString().equals("java"))
                .filter(Files::isExecutable)
                .map(Path::getParent) // bin/
                .map(Path::getParent) // candidate JAVA_HOME
                .distinct()
                .toList();

            // Prefer JAVA_HOME path that has a "release" file
            return javaHomes.stream()
                .filter(jdk -> Files.isRegularFile(jdk.resolve("release")))
                .findFirst()
                .or(() -> javaHomes.stream().findFirst());
        }
    }
}
