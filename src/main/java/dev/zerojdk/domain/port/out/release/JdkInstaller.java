package dev.zerojdk.domain.port.out.release;

import dev.zerojdk.domain.model.release.InstallationRecord;
import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.service.unarchiving.ExtractedArtifact;

public interface JdkInstaller {
    InstallationRecord install(JdkVersion version, ExtractedArtifact artifact);
}
