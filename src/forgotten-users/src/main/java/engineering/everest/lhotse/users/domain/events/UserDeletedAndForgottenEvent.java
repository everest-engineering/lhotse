package engineering.everest.lhotse.users.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Revision("0")
public class UserDeletedAndForgottenEvent {
    private UUID deletedUserId;
    private UUID requestingUserId;
    private String requestReason;
}
