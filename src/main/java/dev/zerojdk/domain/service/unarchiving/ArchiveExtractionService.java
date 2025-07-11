package dev.zerojdk.domain.service.unarchiving;

import dev.zerojdk.adapter.out.unarchiver.event.UnarchivingCompleted;
import dev.zerojdk.adapter.out.unarchiver.event.UnarchivingFailed;
import dev.zerojdk.adapter.out.unarchiver.event.UnarchivingStarted;
import dev.zerojdk.domain.port.out.event.DomainEventPublisher;
import dev.zerojdk.domain.port.out.unarchiving.UnarchiverFactory;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.nio.file.Path;

@RequiredArgsConstructor
public class ArchiveExtractionService {
    private final UnarchiverFactory unarchiverFactory;
    private final DomainEventPublisher publisher;

    public ExtractedArtifact unarchive(File archive, Path target) {
        try {
            publisher.publish(new UnarchivingStarted());

            ExtractedArtifact extractedArtifact = unarchiverFactory.create(archive)
                .extract(target);

            publisher.publish(new UnarchivingCompleted());

            return extractedArtifact;
        } catch (Exception ex) {
            publisher.publish(new UnarchivingFailed(ex));

            throw ex;
        }
    }
}
