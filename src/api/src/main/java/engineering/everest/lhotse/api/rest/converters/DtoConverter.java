package engineering.everest.lhotse.api.rest.converters;

import engineering.everest.lhotse.api.rest.responses.PhotoResponse;
import engineering.everest.lhotse.photos.Photo;
import org.springframework.stereotype.Service;

@Service
public class DtoConverter {

    public PhotoResponse convert(Photo photo) {
        return new PhotoResponse(photo.getId(), photo.getFilename(), photo.getUploadTimestamp());
    }
}
