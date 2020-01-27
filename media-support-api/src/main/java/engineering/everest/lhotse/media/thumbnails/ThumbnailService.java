package engineering.everest.lhotse.media.thumbnails;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public interface ThumbnailService {

    InputStream streamThumbnailForOriginalFile(UUID fileId, int width, int height) throws IOException;
}
