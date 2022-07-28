package engineering.everest.lhotse.photos.domain.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeletePhotoForDeletedUserCommand implements Serializable {
    @TargetAggregateIdentifier
    private UUID photoId;
    private UUID deletedUserId;
}
