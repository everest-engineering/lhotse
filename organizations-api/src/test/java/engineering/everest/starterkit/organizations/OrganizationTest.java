package engineering.everest.starterkit.organizations;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrganizationTest {

    @Test
    void OrganizationClassMustExist() throws ClassNotFoundException {
        final Class<?> organizationClass = Class.forName(getClass().getPackageName() + ".Organization");
        assertNotNull(organizationClass);
    }

}