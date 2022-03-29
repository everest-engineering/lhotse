package engineering.everest.lhotse.users;

import engineering.everest.lhotse.common.domain.User;

import static engineering.everest.lhotse.common.domain.User.ADMIN_ID;

public class UserTestHelper {

    public static final String ADMIN_EMAIL_ADDRESS = "admin@umbrella.com";
    public static final String ADMIN_DISPLAY_NAME = "Admin";
    public static final User ADMIN_USER = new User(ADMIN_ID, null, ADMIN_DISPLAY_NAME, ADMIN_EMAIL_ADDRESS, false);

}
