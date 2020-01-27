package engineering.everest.lhotse.users;


import engineering.everest.lhotse.axon.common.domain.Role;
import engineering.everest.lhotse.axon.common.domain.User;

import java.util.EnumSet;
import java.util.UUID;

import static engineering.everest.lhotse.axon.common.domain.Role.ADMIN;
import static java.util.UUID.fromString;

public class UserTestHelper {

    public static final UUID ADMIN_ID = fromString("00000000-0000-0000-0000-000000000000");

    public static final String ADMIN_USERNAME = "admin@umbrella.com";
    public static final String ADMIN_DISPLAY_NAME = "Admin";
    public static final EnumSet<Role> ADMIN_ROLES = EnumSet.of(ADMIN);
    public static final User ADMIN_USER = new User(ADMIN_ID, null,
            ADMIN_USERNAME, ADMIN_DISPLAY_NAME, ADMIN_USERNAME, false, ADMIN_ROLES);

}
