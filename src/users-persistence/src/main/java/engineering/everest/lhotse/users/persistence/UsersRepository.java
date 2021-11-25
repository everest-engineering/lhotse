package engineering.everest.lhotse.users.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<PersistableUser, UUID> {

    default void createUser(UUID id, UUID organizationId, String displayName, String email, Instant createdOn) {
        save(new PersistableUser(id, organizationId, displayName, email, createdOn));
    }

    List<PersistableUser> findByOrganizationId(UUID organizationId);

    Optional<PersistableUser> findByUsernameIgnoreCase(String username);

    Optional<PersistableUser> findByEmailIgnoreCase(String email);

    void deleteByIdNot(UUID organizationId);
}
