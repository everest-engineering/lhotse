package engineering.everest.lhotse.axon;

import engineering.everest.lhotse.axon.command.validators.EmailAddressValidator;
import engineering.everest.lhotse.axon.command.validators.OrganizationStatusValidator;
import engineering.everest.lhotse.axon.command.validators.UsersUniqueEmailValidator;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalStateException;
import engineering.everest.lhotse.organizations.Organization;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import engineering.everest.lhotse.users.domain.commands.CreateUserCommand;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Set.of;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandValidatingMessageHandlerInterceptorTest {

    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final Organization ORGANIZATION = new Organization();

    @Mock
    private UnitOfWork<? extends CommandMessage<?>> unitOfWork;
    @Mock
    private CommandMessage<?> commandMessage;
    @Mock
    private InterceptorChain interceptorChain;
    @Mock
    private UsersReadService usersReadService;
    @Mock
    private Validator javaBeanValidator;
    @Mock
    private OrganizationsReadService organizationsReadService;

    private EmailAddressValidator emailAddressValidator;
    private UsersUniqueEmailValidator usersUniqueEmailValidator;
    private OrganizationStatusValidator organizationStatusValidator;
    private CommandValidatingMessageHandlerInterceptor commandValidatingMessageHandlerInterceptor;

    @BeforeEach
    void setUp() {
        Mockito.<CommandMessage<?>>when(unitOfWork.getMessage()).thenReturn(commandMessage);
        lenient().when(javaBeanValidator.validate(any())).thenReturn(emptySet());
        emailAddressValidator = new EmailAddressValidator();
        usersUniqueEmailValidator = new UsersUniqueEmailValidator(usersReadService);
        organizationStatusValidator = new OrganizationStatusValidator(organizationsReadService);
        commandValidatingMessageHandlerInterceptor = new CommandValidatingMessageHandlerInterceptor(
                List.of(emailAddressValidator,
                        usersUniqueEmailValidator,
                        organizationStatusValidator),
                javaBeanValidator);
    }


    @Test
    void willValidateSuperInterfaceFirst() {
        var createUserSubClassCommand = mock(CreateUserSubclassCommand.class);
        Mockito.<Class<?>>when(commandMessage.getPayloadType()).thenReturn(CreateUserSubclassCommand.class);
        Mockito.<Object>when(commandMessage.getPayload()).thenReturn(createUserSubClassCommand);

        when(createUserSubClassCommand.getOrganizationId()).thenReturn(ORGANIZATION_ID);
        when(organizationsReadService.getById(ORGANIZATION_ID)).thenThrow(NoSuchElementException.class);

        var exception = assertThrows(TranslatableIllegalStateException.class,
                () -> commandValidatingMessageHandlerInterceptor.handle(unitOfWork, interceptorChain));

        assertEquals("ORGANIZATION_DOES_NOT_EXIST", exception.getMessage());
    }

    @Test
    void willValidateCommandWithMultipleInterfaces() throws Exception {
        CreateUserCommand createUserCommand = mock(CreateUserCommand.class);
        when(createUserCommand.getOrganizationId()).thenReturn(ORGANIZATION_ID);
        Mockito.<Class<?>>when(commandMessage.getPayloadType()).thenReturn(CreateUserCommand.class);
        Mockito.<Object>when(commandMessage.getPayload()).thenReturn(createUserCommand);
        when(organizationsReadService.getById(ORGANIZATION_ID)).thenReturn(ORGANIZATION);

        commandValidatingMessageHandlerInterceptor.handle(unitOfWork, interceptorChain);

        verify(createUserCommand, times(2)).getEmailAddress();
        verify(createUserCommand, times(1)).getOrganizationId();
    }

    @Test
    void willThrow_WhenJavaBeanValidatorFails() {
        CreateUserCommand createUserCommand = mock(CreateUserCommand.class);

        Mockito.<Object>when(javaBeanValidator.validate(any())).thenReturn(of(mock(ConstraintViolation.class)));
        Mockito.<Class<?>>when(commandMessage.getPayloadType()).thenReturn(CreateUserCommand.class);
        Mockito.<Object>when(commandMessage.getPayload()).thenReturn(createUserCommand);

        assertThrows(ConstraintViolationException.class,
                () -> commandValidatingMessageHandlerInterceptor.handle(unitOfWork, interceptorChain));
    }

    @Test
    void willValidateCommandWithSuperClass() throws Exception {
        CreateUserSubclassCommand createUserSubclassCommand = mock(CreateUserSubclassCommand.class);
        when(createUserSubclassCommand.getOrganizationId()).thenReturn(ORGANIZATION_ID);
        Mockito.<Class<?>>when(commandMessage.getPayloadType()).thenReturn(CreateUserSubclassCommand.class);
        Mockito.<Object>when(commandMessage.getPayload()).thenReturn(createUserSubclassCommand);
        when(organizationsReadService.getById(ORGANIZATION_ID)).thenReturn(ORGANIZATION);

        commandValidatingMessageHandlerInterceptor.handle(unitOfWork, interceptorChain);

        verify(createUserSubclassCommand, times(2)).getEmailAddress();
        verify(createUserSubclassCommand, times(2)).getOrganizationId();
    }
}
