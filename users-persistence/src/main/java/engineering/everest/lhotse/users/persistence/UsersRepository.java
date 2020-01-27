package engineering.everest.lhotse.users.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends MongoRepository<PersistableUser, UUID> {

    default void createUser(UUID id, UUID organizationId, String displayName, String email, String hashedPassword, Instant createdOn) {
        save(new PersistableUser(id, organizationId, displayName, email, hashedPassword, createdOn));
    }

    List<PersistableUser> findByOrganizationId(UUID organizationId);

    @Query("{ 'roles': { $all: ['ORGANIZATION_ADMIN']} }")
    List<PersistableUser> findAdmins();

    Optional<PersistableUser> findByUsernameIgnoreCase(String username);

    Optional<PersistableUser> findByEmailIgnoreCase(String email);
}
