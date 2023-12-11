package engineering.everest.lhotse.photos.services;

import engineering.everest.lhotse.common.AuthenticatedUser;
import engineering.everest.lhotse.common.RandomFieldsGenerator;
import engineering.everest.lhotse.photos.domain.commands.RegisterUploadedPhotoCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultPhotosService implements PhotosService {

    private final CommandGateway commandGateway;
    private final RandomFieldsGenerator randomFieldsGenerator;

    private final AuthenticatedUser authenticatedUser;

    public DefaultPhotosService(CommandGateway commandGateway,
                                RandomFieldsGenerator randomFieldsGenerator,
                                AuthenticatedUser authenticatedUser) {
        this.commandGateway = commandGateway;
        this.randomFieldsGenerator = randomFieldsGenerator;
        this.authenticatedUser = authenticatedUser;
    }

    @Override
    public UUID registerUploadedPhoto(UUID persistedFileId, String filename) {
        var photoId = randomFieldsGenerator.genRandomUUID();
        var requestingUserId = authenticatedUser.getUserId();
        commandGateway.sendAndWait(new RegisterUploadedPhotoCommand(photoId, requestingUserId, persistedFileId, filename));
        return photoId;
    }
}
