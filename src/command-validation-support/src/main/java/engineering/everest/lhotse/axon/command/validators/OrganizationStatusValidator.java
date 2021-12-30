package engineering.everest.lhotse.axon.command.validators;

import engineering.everest.lhotse.axon.command.AxonCommandExecutionExceptionFactory;
import engineering.everest.lhotse.axon.command.validation.OrganizationStatusValidatableCommand;
import engineering.everest.lhotse.axon.command.validation.Validates;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalStateException;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

import static engineering.everest.lhotse.i18n.MessageKeys.ORGANIZATION_DOES_NOT_EXIST;
import static engineering.everest.lhotse.i18n.MessageKeys.ORGANIZATION_IS_DEREGISTERED;

@Component
public class OrganizationStatusValidator implements Validates<OrganizationStatusValidatableCommand> {

    private final OrganizationsReadService organizationsReadService;
    private final AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory;

    public OrganizationStatusValidator(OrganizationsReadService organizationsReadService,
                                       AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        this.organizationsReadService = organizationsReadService;
        this.axonCommandExecutionExceptionFactory = axonCommandExecutionExceptionFactory;
    }

    @Override
    public void validate(OrganizationStatusValidatableCommand command) {
        try {
            var organization = organizationsReadService.getById(command.getOrganizationId());
            if (organization.isDisabled()) {
                axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                    new TranslatableIllegalStateException(ORGANIZATION_IS_DEREGISTERED, command.getOrganizationId()));
            }
        } catch (NoSuchElementException e) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalStateException(ORGANIZATION_DOES_NOT_EXIST, e, command.getOrganizationId()));
        }
    }
}
