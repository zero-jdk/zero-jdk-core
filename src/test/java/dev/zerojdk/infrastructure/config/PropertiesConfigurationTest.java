package dev.zerojdk.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SystemStubsExtension.class)
class PropertiesConfigurationTest {
    @TempDir
    private Path tempDir;

    @Test
    void loadsSimpleKeyValuePairs() throws Exception {
        // Given
        Path appPropertiesPath = tempDir.resolve("app.properties");
        Files.writeString(appPropertiesPath, """
            # a comment
            foo = bar
            baz=qux
            """,
            StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        // When
        PropertiesConfiguration actual = PropertiesConfiguration.from(appPropertiesPath);

        // Then
        assertThat(actual.getString("foo"))
            .isEqualTo("bar");
        assertThat(actual.getString("baz"))
            .isEqualTo("qux");
        assertThat(actual.getString("missing"))
            .isNull();
    }


    @Test
    void lastDuplicateEntryWins() throws Exception {
        // Given
        Path appPropertiesPath = tempDir.resolve("app.properties");
        Files.writeString(appPropertiesPath, """
            key=first
            key=second
            """,
            StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        // When
        PropertiesConfiguration actual = PropertiesConfiguration.from(appPropertiesPath);

        // Then
        assertThat(actual.getString("key"))
            .isEqualTo("second");
    }

    @Test
    void savesInInsertionOrder() throws Exception {
        // Given
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("alpha", "1");
        config.addProperty("beta",  "2");
        config.addProperty("gamma", "3");

        Path appPropertiesPath = tempDir.resolve("app.properties");

        // When
        config.save(appPropertiesPath);

        // Then
        List<String> lines = Files.readAllLines(appPropertiesPath);

        assertThat(lines).containsExactly(
            "alpha=1",
            "beta=2",
            "gamma=3"
        );
    }

}