package engineering.everest.lhotse.photos.domain;

import engineering.everest.lhotse.photos.domain.commands.DeletePhotoForDeletedUserCommand;
import engineering.everest.lhotse.photos.services.PhotosReadService;
import engineering.everest.lhotse.users.domain.events.UserDeletedAndForgottenEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.serialization.Revision;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;

@Saga
@Revision("0")
@Slf4j
public class UserDeletedSaga implements Serializable {

    @StartSaga
    @EndSaga
    @SagaEventHandler(associationProperty = "deletedUserId")
    public void on(UserDeletedAndForgottenEvent event, PhotosReadService photosReadService, CommandGateway commandGateway) {
        LOGGER.debug("Deleting photos for deleted user {}", event.getDeletedUserId());
        photosReadService.getAllPhotos(event.getDeletedUserId(), Pageable.unpaged())
            .forEach(photo -> commandGateway.send(new DeletePhotoForDeletedUserCommand(photo.getId(), event.getDeletedUserId())));
    }
}
