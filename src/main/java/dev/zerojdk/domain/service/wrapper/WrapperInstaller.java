package dev.zerojdk.domain.service.wrapper;

import dev.zerojdk.domain.model.context.GlobalLayoutContext;
import dev.zerojdk.domain.model.context.LayoutContext;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.model.wrapper.WrapperConfig;
import dev.zerojdk.domain.port.out.wrapper.WrapperReleaseResolver;
import dev.zerojdk.domain.port.out.wrapper.WrapperConfigRepository;
import dev.zerojdk.domain.port.out.wrapper.WrapperScriptRepository;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

@RequiredArgsConstructor
public class WrapperInstaller {
    private final WrapperConfigRepository wrapperConfigRepository;
    private final WrapperScriptRepository wrapperScriptRepository;
    private final WrapperReleaseResolver wrapperReleaseResolver;
    private final WrapperScriptGenerator wrapperScriptGenerator;
    private final BinaryInstaller binaryInstaller;

    public void install(Platform platform, String version, Path executable, LayoutContext layoutContext) {
        if (layoutContext instanceof GlobalLayoutContext) {
            // TODO: proper exception
            throw new UnsupportedOperationException("Cannot install global wrappers");
        }

        WrapperConfig wrapperConfig = wrapperConfigRepository.read(layoutContext)
            .orElseGet(() -> wrapperConfigRepository.write(layoutContext,
                new WrapperConfig(wrapperReleaseResolver.resolveUrl(platform, version))));

        wrapperScriptRepository.save(layoutContext,
            wrapperScriptGenerator.generateScript(layoutContext, wrapperConfig.url()));

        binaryInstaller.install(executable, layoutContext);
    }
}
