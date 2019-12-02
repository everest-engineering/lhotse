package engineering.everest.starterkit.axon.media.thumbnails.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "thumbnails")
public
class PersistableThumbnailMapping {

    @Id
    private UUID sourceFileId;
    private List<PersistableThumbnail> thumbnails = new ArrayList<>();

    public void addThumbnail(UUID thumbnailFileId, int width, int height) {
        thumbnails.add(new PersistableThumbnail(thumbnailFileId, width, height));
    }
}
