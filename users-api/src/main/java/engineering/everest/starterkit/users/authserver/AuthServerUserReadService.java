package engineering.everest.starterkit.users.authserver;

public interface AuthServerUserReadService {

    AuthServerUser getByUsername(String username);

}
