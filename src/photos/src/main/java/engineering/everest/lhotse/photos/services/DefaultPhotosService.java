package engineering.everest.lhotse.photos.services;

import engineering.everest.lhotse.common.RandomFieldsGenerator;
import engineering.everest.lhotse.photos.domain.commands.RegisterUploadedPhotoCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultPhotosService implements PhotosService {

    private final CommandGateway commandGateway;
    private final RandomFieldsGenerator randomFieldsGenerator;

    public DefaultPhotosService(CommandGateway commandGateway, RandomFieldsGenerator randomFieldsGenerator) {
        this.commandGateway = commandGateway;
        this.randomFieldsGenerator = randomFieldsGenerator;
    }

    @Override
    public UUID registerUploadedPhoto(UUID requestingUserId, UUID backingFileId, String filename) {
        var photoId = randomFieldsGenerator.genRandomUUID();
        commandGateway.sendAndWait(new RegisterUploadedPhotoCommand(photoId, requestingUserId, backingFileId, filename));
        return photoId;
    }
}
