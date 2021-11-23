package engineering.everest.lhotse.users.domain.events;

import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.axonframework.serialization.Revision;

import engineering.everest.axon.cryptoshredding.annotations.EncryptionKeyIdentifier;
import engineering.everest.lhotse.axon.common.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Revision("0")
public class UserRolesAddedByAdminEvent {
    @EncryptionKeyIdentifier
    private UUID userId;
    private Set<Role> roles;

    @NotNull
    private UUID requestingUserId;
}
