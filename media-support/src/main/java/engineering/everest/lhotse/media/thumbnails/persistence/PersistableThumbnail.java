package engineering.everest.lhotse.media.thumbnails.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public
class PersistableThumbnail {

    private UUID thumbnailFileId;
    private int width;
    private int height;

}
