package engineering.everest.starterkit.organizations.services;

import engineering.everest.starterkit.axon.users.services.UsersReadService;
import engineering.everest.starterkit.organizations.Organization;
import engineering.everest.starterkit.organizations.OrganizationAddress;
import engineering.everest.starterkit.organizations.persistence.OrganizationsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureDataMongo
@ExtendWith(SpringExtension.class)
@ComponentScan(basePackages = "engineering.everest.starterkit.organizations")
class OrganizationsReadServiceIntegrationTest {

    private static final UUID ORGANIZATION_ID_1 = randomUUID();
    private static final UUID ORGANIZATION_ID_2 = randomUUID();
    private static final UUID ORGANIZATION_ID_3 = randomUUID();
    private static final String ORGANIZATION_NAME_1 = "OrganizationNameOne";
    private static final String ORGANIZATION_NAME_2 = "OrganizationNameTwo";
    private static final String ORGANIZATION_NAME_3 = "OrganizationNameThree";
    private static final Instant ORGANIZATION_CREATED_ON_1 = Instant.ofEpochSecond(100L);
    private static final Instant ORGANIZATION_CREATED_ON_2 = Instant.ofEpochSecond(700L);
    private static final Instant ORGANIZATION_CREATED_ON_3 = Instant.ofEpochSecond(5500L);
    private static final String ORGANIZATION_ADDRESS_STREET_1 = "street-1";
    private static final String ORGANIZATION_ADDRESS_STREET_2 = "street-2";
    private static final String ORGANIZATION_ADDRESS_STREET_3 = "street-3";
    private static final String ORGANIZATION_CITY_1 = "city-1";
    private static final String ORGANIZATION_CITY_2 = "city-2";
    private static final String ORGANIZATION_CITY_3 = "city-3";
    private static final String ORGANIZATION_STATE_1 = "state-1";
    private static final String ORGANIZATION_STATE_2 = "state-2";
    private static final String ORGANIZATION_STATE_3 = "state-3";
    private static final String ORGANIZATION_COUNTRY_1 = "country-1";
    private static final String ORGANIZATION_COUNTRY_2 = "country-2";
    private static final String ORGANIZATION_COUNTRY_3 = "country-3";
    private static final String ORGANIZATION_POSTAL_CODE_1 = "postal-1";
    private static final String ORGANIZATION_POSTAL_CODE_2 = "postal-2";
    private static final String ORGANIZATION_ADDRESS_POSTAL_CODE_3 = "postal-3";
    private static final String ORGANIZATION_EMAIL_1 = "org-email-1";
    private static final String ORGANIZATION_EMAIL_2 = "org-email-2";
    private static final String ORGANIZATION_EMAIL_3 = "org-email-3";
    private static final String ORGANIZATION_WEBSITE_1 = "org-website-1";
    private static final String ORGANIZATION_WEBSITE_2 = "org-website-2";
    private static final String ORGANIZATION_WEBSITE_3 = "org-website-3";
    private static final String CONTACT_NAME_1 = "contact-name-1";
    private static final String CONTACT_NAME_2 = "contact-name-2";
    private static final String CONTACT_NAME_3 = "contact-name-3";
    private static final String PHONE_NUMBER_1 = "phone-number-1";
    private static final String PHONE_NUMBER_2 = "phone-number-2";
    private static final String PHONE_NUMBER_3 = "phone-number-3";

    private static final OrganizationAddress ORGANIZATION_ADDRESS_1 = new OrganizationAddress(ORGANIZATION_ADDRESS_STREET_1, ORGANIZATION_CITY_1,
            ORGANIZATION_STATE_1, ORGANIZATION_COUNTRY_1, ORGANIZATION_POSTAL_CODE_1);
    private static final OrganizationAddress ORGANIZATION_ADDRESS_2 = new OrganizationAddress(ORGANIZATION_ADDRESS_STREET_2, ORGANIZATION_CITY_2,
            ORGANIZATION_STATE_2, ORGANIZATION_COUNTRY_2, ORGANIZATION_POSTAL_CODE_2);
    private static final OrganizationAddress ORGANIZATION_ADDRESS_3 = new OrganizationAddress(ORGANIZATION_ADDRESS_STREET_3, ORGANIZATION_CITY_3,
            ORGANIZATION_STATE_3, ORGANIZATION_COUNTRY_3, ORGANIZATION_ADDRESS_POSTAL_CODE_3);

    private static final Organization ORGANIZATION_1 = new Organization(ORGANIZATION_ID_1, ORGANIZATION_NAME_1, ORGANIZATION_ADDRESS_1, ORGANIZATION_WEBSITE_1, CONTACT_NAME_1, PHONE_NUMBER_1, ORGANIZATION_EMAIL_1, false);
    private static final Organization ORGANIZATION_2 = new Organization(ORGANIZATION_ID_2, ORGANIZATION_NAME_2, ORGANIZATION_ADDRESS_2, ORGANIZATION_WEBSITE_2, CONTACT_NAME_2, PHONE_NUMBER_2, ORGANIZATION_EMAIL_2, false);
    private static final Organization ORGANIZATION_3 = new Organization(ORGANIZATION_ID_3, ORGANIZATION_NAME_3, ORGANIZATION_ADDRESS_3, ORGANIZATION_WEBSITE_3, CONTACT_NAME_3, PHONE_NUMBER_3, ORGANIZATION_EMAIL_3, false);

    @Autowired
    private OrganizationsRepository organizationsRepository;
    @Autowired
    private DefaultOrganizationsReadService organizationsReadService;
    @MockBean
    private UsersReadService usersReadService;

    @BeforeEach
    void setUp() {
        organizationsRepository.createOrganization(ORGANIZATION_ID_1, ORGANIZATION_NAME_1, ORGANIZATION_ADDRESS_1, ORGANIZATION_WEBSITE_1,
                CONTACT_NAME_1, PHONE_NUMBER_1, ORGANIZATION_EMAIL_1, ORGANIZATION_CREATED_ON_1);
        organizationsRepository.createOrganization(ORGANIZATION_ID_2, ORGANIZATION_NAME_2, ORGANIZATION_ADDRESS_2, ORGANIZATION_WEBSITE_2,
                CONTACT_NAME_2, PHONE_NUMBER_2, ORGANIZATION_EMAIL_2, ORGANIZATION_CREATED_ON_2);
        organizationsRepository.createOrganization(ORGANIZATION_ID_3, ORGANIZATION_NAME_3, ORGANIZATION_ADDRESS_3, ORGANIZATION_WEBSITE_3,
                CONTACT_NAME_3, PHONE_NUMBER_3, ORGANIZATION_EMAIL_3, ORGANIZATION_CREATED_ON_3);
    }

    @Test
    void exists_WillBeTrue_WhenOrganizationExists() {
        assertTrue(organizationsReadService.exists(ORGANIZATION_ID_1));
    }

    @Test
    void exists_WillBeFalse_WhenOrganizationDoesNotExist() {
        assertFalse(organizationsReadService.exists(randomUUID()));
    }

    @Test
    void getOrganizationList_WillReturnAllOrganizations() {
        assertEquals(asList(ORGANIZATION_1, ORGANIZATION_2, ORGANIZATION_3),
                organizationsReadService.getOrganizations());
    }

    @Test
    void getOrganization_WillReturnOrganization_WhenItExists() {
        assertEquals(ORGANIZATION_1, organizationsReadService.getById(ORGANIZATION_ID_1));
    }

    @Test
    void getOrganization_WillFail_WhenOrganizationDoesNotExist() {
        assertThrows(NoSuchElementException.class, () -> {
            organizationsReadService.getById(randomUUID());
        });
    }
}
