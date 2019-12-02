package engineering.everest.starterkit.axon.users.persistence;

import engineering.everest.starterkit.axon.common.domain.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends MongoRepository<PersistableUser, UUID> {

    default void createUser(UUID id, UUID organizationId, String displayName, String email, String hashedPassword, Instant createdOn) {
        save(new PersistableUser(id, organizationId, displayName, email, hashedPassword, createdOn));
    }

    default void createHelpSessionGuestUser(UUID guestUserId, UUID organizationId, String email, String hashedPassword, Instant createdOn) {
        save(new PersistableUser(guestUserId, organizationId, email, hashedPassword,
                "Guest", false, EnumSet.of(Role.GUEST), createdOn));
    }

    List<PersistableUser> findByOrganizationId(UUID organizationId);

    @Query("{ 'roles': { $all: ['ORGANIZATION_EXPERT']} }")
    List<PersistableUser> findExperts();

    @Query("{ 'organizationId': ?0 , 'roles': { $all: ['ORGANIZATION_EXPERT']} }")
    List<PersistableUser> findOrganizationExperts(UUID organizationId);

    @Query("{ 'roles': { $all: ['ORGANIZATION_ADMIN']} }")
    List<PersistableUser> findAdmins();

    @Query("{ 'organizationId': ?0 , 'roles': { $all: ['ORGANIZATION_ADMIN']} }")
    List<PersistableUser> findOrganizationAdmins(UUID organizationId);

    @Query("{ 'roles': { $all: ['HELP_SESSION_GUEST'] }, 'createdOn': { $lt: ?0 } }")
    List<PersistableUser> findHelpSessionGuestsCreatedBefore(Instant createdOn);

    Optional<PersistableUser> findByUsernameIgnoreCase(String username);

    Optional<PersistableUser> findByEmailIgnoreCase(String email);
}
