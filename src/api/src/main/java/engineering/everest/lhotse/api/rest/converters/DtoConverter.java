package engineering.everest.lhotse.api.rest.converters;

import engineering.everest.lhotse.api.rest.responses.PhotoResponse;
import engineering.everest.lhotse.api.rest.responses.UserResponse;
import engineering.everest.lhotse.common.domain.User;
import engineering.everest.lhotse.photos.Photo;
import org.springframework.stereotype.Service;

@Service
public class DtoConverter {

    public UserResponse convert(User user) {
        return new UserResponse(user.getId(), user.getOrganizationId(), user.getDisplayName(),
            user.getEmailAddress(), user.isDisabled());
    }

    public PhotoResponse convert(Photo photo) {
        return new PhotoResponse(photo.getId(), photo.getFilename(), photo.getUploadTimestamp());
    }
}
