package engineering.everest.lhotse.registrations.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface PendingRegistrationsRepository extends JpaRepository<PersistablePendingRegistration, UUID> {

    default void createPendingRegistration(UUID registeringOrganizationId,
                                           UUID confirmationCode,
                                           UUID registeringUserId,
                                           String registeringUserEmail,
                                           Instant registeredOn) {
        save(new PersistablePendingRegistration(registeringOrganizationId, confirmationCode,
                registeringUserId, registeringUserEmail, registeredOn));
    }
}
