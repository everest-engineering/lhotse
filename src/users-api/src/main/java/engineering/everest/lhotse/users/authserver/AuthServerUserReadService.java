package engineering.everest.lhotse.users.authserver;

public interface AuthServerUserReadService {

    AuthServerUser getByUsername(String username);

}
