package dev.zerojdk.domain.port.out.layout;

import java.io.IOException;
import java.nio.file.Path;

public interface CatalogStorageLayout {
    Path metadataFile();
    Path catalogFile();

    Path ensureCatalogStorageDirectory() throws IOException;
}
