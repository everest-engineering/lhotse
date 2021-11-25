package engineering.everest.lhotse.axon.command.validators;

import engineering.everest.lhotse.axon.command.validation.OrganizationStatusValidatableCommand;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalStateException;
import engineering.everest.lhotse.organizations.Organization;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationStatusValidatorTest {

    private static final UUID ORGANIZATION_ID_1 = UUID.randomUUID();
    private static final UUID ORGANIZATION_ID_2 = UUID.randomUUID();
    private static final Organization ORGANIZATION_IN_GOOD_STANDING = new Organization(ORGANIZATION_ID_1, "organization-name", null, null, null, null, null, false);
    private static final Organization DISABLED_ORGANIZATION = new Organization(ORGANIZATION_ID_2, "organization-name", null, null, null, null, null, true);

    private OrganizationStatusValidator organizationStatusValidator;

    @Mock
    private OrganizationsReadService organizationsReadService;

    @BeforeEach
    void setUp() {
        organizationStatusValidator = new OrganizationStatusValidator(organizationsReadService);
    }

    @Test
    void validate_WillPass_WhenOrganizationExistsAndIsEnabled() {
        when(organizationsReadService.getById(ORGANIZATION_ID_1)).thenReturn(ORGANIZATION_IN_GOOD_STANDING);

        organizationStatusValidator.validate((OrganizationStatusValidatableCommand) () -> ORGANIZATION_ID_1);
    }

    @Test
    void validate_WillFail_WhenOrganizationIsDisabled() {
        when(organizationsReadService.getById(ORGANIZATION_ID_2)).thenReturn(DISABLED_ORGANIZATION);

        var thrownException = assertThrows(TranslatableIllegalStateException.class, () ->
                organizationStatusValidator.validate((OrganizationStatusValidatableCommand) () -> ORGANIZATION_ID_2));
        assertEquals("ORGANIZATION_IS_DEREGISTERED", thrownException.getMessage());
    }

    @Test
    void validate_WillFail_WhenOrganizationDoesNotExist() {
        when(organizationsReadService.getById(ORGANIZATION_ID_1)).thenThrow(NoSuchElementException.class);

        var thrownException = assertThrows(TranslatableIllegalStateException.class, () ->
                organizationStatusValidator.validate((OrganizationStatusValidatableCommand) () -> ORGANIZATION_ID_1));
        assertEquals("ORGANIZATION_DOES_NOT_EXIST", thrownException.getMessage());
    }
}
