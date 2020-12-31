package engineering.everest.lhotse.axon.command.validators;

import engineering.everest.lhotse.axon.command.validation.OrganizationStatusValidatableCommand;
import engineering.everest.lhotse.axon.command.validation.Validates;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalStateException;
import engineering.everest.lhotse.organizations.Organization;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

import static engineering.everest.lhotse.i18n.TranslatingValidator.isValidState;

@Component
public class OrganizationStatusValidator implements Validates<OrganizationStatusValidatableCommand> {

    private final OrganizationsReadService organizationsReadService;

    public OrganizationStatusValidator(OrganizationsReadService organizationsReadService) {
        this.organizationsReadService = organizationsReadService;
    }

    @Override
    public void validate(OrganizationStatusValidatableCommand command) {
        Organization organization;
        try {
            organization = organizationsReadService.getById(command.getOrganizationId());
        } catch (NoSuchElementException e) {
            throw new TranslatableIllegalStateException("ORGANIZATION_DOES_NOT_EXIST", e, command.getOrganizationId());
        }
        isValidState(!organization.isDisabled(), "ORGANIZATION_IS_DEREGISTERED", command.getOrganizationId());
    }
}
