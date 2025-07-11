package dev.zerojdk.domain.service.sync;

import dev.zerojdk.domain.model.context.LayoutContext;
import dev.zerojdk.domain.service.config.JdkConfigService;
import dev.zerojdk.domain.service.install.JdkInstallService;
import dev.zerojdk.domain.service.release.JdkReleaseService;
import lombok.RequiredArgsConstructor;

import dev.zerojdk.domain.model.Platform;

@RequiredArgsConstructor
public class ManifestSyncService {
    private final JdkConfigService jdkConfigService;
    private final JdkInstallService jdkInstallService;

    public void sync(Platform platform, LayoutContext layoutContext) {


        String identifier = jdkConfigService.getActiveVersion(layoutContext);

        jdkInstallService.install(platform, identifier);
    }
}
