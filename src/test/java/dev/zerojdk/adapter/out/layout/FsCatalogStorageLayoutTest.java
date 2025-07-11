package dev.zerojdk.adapter.out.layout;

import dev.zerojdk.domain.model.context.GlobalLayoutContext;
import dev.zerojdk.domain.port.out.layout.BaseLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SystemStubsExtension.class)
class FsCatalogStorageLayoutTest {
    @TempDir
    private File tempDir;

    private FsCatalogStorageLayout storageLayout;

    @BeforeEach
    void setup() {
        BaseLayout baseLayout = mock(BaseLayout.class);
        GlobalLayoutContext context = new GlobalLayoutContext();

        when(baseLayout.baseDirectory(context))
            .thenReturn(tempDir.toPath());
        when(baseLayout.ensureBaseDirectory(context))
            .thenReturn(tempDir.toPath());

        storageLayout = new FsCatalogStorageLayout(baseLayout);
    }

    @Test
    void ensureCatalogStorageDirectory() throws IOException {
        // Given
        assertThat(storageLayout.metadataFile().getParent())
            .doesNotExist();

        // When
        storageLayout.ensureCatalogStorageDirectory();

        // Then
        assertThat(storageLayout.metadataFile().getParent())
            .exists();
    }

    @Test
    void metadataFileReturnsPathInsideCatalogDirectory() {
        // When
        Path metadataFile = storageLayout.metadataFile();

        // Then
        assertThat(metadataFile)
            .hasFileName("catalog.properties");
        assertThat(metadataFile.getParent())
            .hasFileName("catalog");
    }

    @Test
    void catalogFileReturnsPathInsideCatalogDirectory() {
        // When
        Path catalogFile = storageLayout.catalogFile();

        // Then
        assertThat(catalogFile)
            .hasFileName("catalog.json");
        assertThat(catalogFile.getParent())
            .hasFileName("catalog");
    }
}