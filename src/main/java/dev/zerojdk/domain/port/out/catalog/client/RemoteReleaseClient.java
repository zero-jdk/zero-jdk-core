package dev.zerojdk.domain.port.out.catalog.client;

import dev.zerojdk.domain.model.catalog.client.Asset;
import dev.zerojdk.domain.model.catalog.client.Release;
import dev.zerojdk.domain.port.out.download.ProgressListener;

import java.io.File;

public interface RemoteReleaseClient {
    Release getLatestRelease();
    File downloadReleaseAsset(Asset asset, ProgressListener progressListener);
}
