package engineering.everest.lhotse.axon;

import engineering.everest.lhotse.axon.command.AxonCommandExecutionExceptionFactory;
import engineering.everest.lhotse.axon.command.validators.EmailAddressValidator;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.Validator;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CommandValidatingMessageHandlerInterceptorTest {

    private static final UUID ORGANIZATION_ID = randomUUID();

    @Mock
    private UnitOfWork<? extends CommandMessage<?>> unitOfWork;
    @Mock
    private CommandMessage<?> commandMessage;
    @Mock
    private InterceptorChain interceptorChain;
    @Mock
    private Validator javaBeanValidator;
    @Mock

    private EmailAddressValidator emailAddressValidator;
    private CommandValidatingMessageHandlerInterceptor commandValidatingMessageHandlerInterceptor;
    private AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory;

    @BeforeEach
    void setUp() {
        Mockito.<CommandMessage<?>>when(unitOfWork.getMessage()).thenReturn(commandMessage);
        lenient().when(javaBeanValidator.validate(any())).thenReturn(emptySet());
        axonCommandExecutionExceptionFactory = new AxonCommandExecutionExceptionFactory();
        emailAddressValidator = new EmailAddressValidator(axonCommandExecutionExceptionFactory);
        commandValidatingMessageHandlerInterceptor = new CommandValidatingMessageHandlerInterceptor(
            List.of(emailAddressValidator),
            javaBeanValidator);
    }

    // @Test
    // void willValidateSuperInterfaceFirst() {
    // var createUserSubClassCommand = mock(CreateUserSubclassTestCommand.class);
    // Mockito.<Class<?>>when(commandMessage.getPayloadType()).thenReturn(CreateUserSubclassTestCommand.class);
    // Mockito.<Object>when(commandMessage.getPayload()).thenReturn(createUserSubClassCommand);
    //
    // when(createUserSubClassCommand.getOrganizationId()).thenReturn(ORGANIZATION_ID);
    // when(organizationsReadService.getById(ORGANIZATION_ID)).thenThrow(NoSuchElementException.class);
    //
    // var exception = assertThrows(CommandExecutionException.class,
    // () -> commandValidatingMessageHandlerInterceptor.handle(unitOfWork, interceptorChain));
    //
    // assertEquals("ORGANIZATION_DOES_NOT_EXIST", exception.getMessage());
    // var details = exception.getDetails().orElseThrow();
    // assertEquals(TranslatableIllegalStateException.class, details.getClass());
    // assertEquals("ORGANIZATION_DOES_NOT_EXIST", ((TranslatableException) details).getMessage());
    // }
    //
    // @Test
    // void willValidateCommandWithMultipleInterfaces() throws Exception {
    // var createUserCommand = mock(CreateOrganizationUserCommand.class);
    // when(createUserCommand.getOrganizationId()).thenReturn(ORGANIZATION_ID);
    // Mockito.<Class<?>>when(commandMessage.getPayloadType()).thenReturn(CreateOrganizationUserCommand.class);
    // Mockito.<Object>when(commandMessage.getPayload()).thenReturn(createUserCommand);
    // when(organizationsReadService.getById(ORGANIZATION_ID)).thenReturn(ORGANIZATION);
    //
    // commandValidatingMessageHandlerInterceptor.handle(unitOfWork, interceptorChain);
    //
    // verify(createUserCommand, times(2)).getEmailAddress();
    // verify(createUserCommand, times(1)).getOrganizationId();
    // }
    //
    // @Test
    // void willThrow_WhenJavaBeanValidatorFails() {
    // var createUserCommand = mock(CreateOrganizationUserCommand.class);
    //
    // Mockito.<Object>when(javaBeanValidator.validate(any())).thenReturn(of(mock(ConstraintViolation.class)));
    // Mockito.<Class<?>>when(commandMessage.getPayloadType()).thenReturn(CreateOrganizationUserCommand.class);
    // Mockito.<Object>when(commandMessage.getPayload()).thenReturn(createUserCommand);
    //
    // assertThrows(ConstraintViolationException.class,
    // () -> commandValidatingMessageHandlerInterceptor.handle(unitOfWork, interceptorChain));
    // }
    //
    // @Test
    // void willValidateCommandWithSuperClass() throws Exception {
    // var createUserSubclassTestCommand = mock(CreateUserSubclassTestCommand.class);
    // when(createUserSubclassTestCommand.getOrganizationId()).thenReturn(ORGANIZATION_ID);
    // Mockito.<Class<?>>when(commandMessage.getPayloadType()).thenReturn(CreateUserSubclassTestCommand.class);
    // Mockito.<Object>when(commandMessage.getPayload()).thenReturn(createUserSubclassTestCommand);
    // when(organizationsReadService.getById(ORGANIZATION_ID)).thenReturn(ORGANIZATION);
    //
    // commandValidatingMessageHandlerInterceptor.handle(unitOfWork, interceptorChain);
    //
    // verify(createUserSubclassTestCommand, times(2)).getEmailAddress();
    // verify(createUserSubclassTestCommand, times(2)).getOrganizationId();
    // }
}
