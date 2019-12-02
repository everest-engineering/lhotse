package engineering.everest.starterkit.axon.users.authserver;

public interface AuthServerUserReadService {

    AuthServerUser getByUsername(String username);

}
