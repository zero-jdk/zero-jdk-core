package dev.zerojdk.adapter.out.github.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import dev.zerojdk.domain.model.catalog.client.Asset;
import dev.zerojdk.domain.model.catalog.client.Release;
import dev.zerojdk.domain.port.out.download.DownloadService;
import dev.zerojdk.domain.port.out.download.ProgressListener;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitHubReleaseClientTest {
    private WireMockServer wireMock;
    private GitHubReleaseClient client;

    @Mock
    private DownloadService downloadService;

    @BeforeEach
    void setup() throws IOException {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();

        wireMock.stubFor(get(urlEqualTo("/releases/latest"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(loadJsonFromResource("download/github/release.json"))));

        client = new GitHubReleaseClient(downloadService,
            wireMock.url("/releases/latest"));
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void getLatestReleaseReturnsExpectedValues() {
        // When
        Release release = client.getLatestRelease();

        // Then
        assertThat(release.tagName())
            .isEqualTo("1.0.0");

        assertThat(release.assets())
            .extracting(Asset::browserDownloadUrl)
            .containsExactly("https://github.com/julien-may/zero-jdk-catalog/releases/download/1.0.0/catalog.json.gz");
    }

    @Test
    void downloadReleaseAssetDelegatesToDownloadService() throws Exception {
        // Given
        Asset asset = new Asset("https://example.com/catalog.json.gz");

        ProgressListener listener = mock(ProgressListener.class);
        File file = File.createTempFile("catalog", ".gz");

        when(downloadService.download(asset.browserDownloadUrl(), listener))
            .thenReturn(file);

        // When
        File actual = client.downloadReleaseAsset(asset, listener);

        // Then
        assertThat(actual)
            .isEqualTo(file);
    }

    private String loadJsonFromResource(String path) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            assert in != null : "Resource not found: " + path;
            return new Scanner(in, StandardCharsets.UTF_8).useDelimiter("\\A").next();
        }
    }
}
