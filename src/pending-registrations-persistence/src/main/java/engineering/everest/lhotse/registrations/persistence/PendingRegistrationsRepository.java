package engineering.everest.lhotse.registrations.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface PendingRegistrationsRepository extends JpaRepository<PersistablePendingRegistration, UUID> {

    default void createPendingRegistration(UUID registrationConfirmationCode,
                                           UUID registeringOrganizationId,
                                           UUID registeringUserId,
                                           String registeringUserEmail,
                                           Instant registeredOn) {
        save(new PersistablePendingRegistration(registrationConfirmationCode, registeringOrganizationId, registeringUserId,
                registeringUserEmail, registeredOn));
    }

    PersistablePendingRegistration findByOrganizationId(UUID organizationId);
}
