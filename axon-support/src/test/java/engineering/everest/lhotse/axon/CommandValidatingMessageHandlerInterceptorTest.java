package engineering.everest.lhotse.axon;

import engineering.everest.lhotse.axon.common.domain.Role;
import engineering.everest.lhotse.axon.common.domain.User;
import engineering.everest.lhotse.users.services.UsersReadService;
import engineering.everest.lhotse.axon.command.validators.EmailAddressValidator;
import engineering.everest.lhotse.axon.command.validators.UsersUniqueEmailValidator;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.Validator;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CommandValidatingMessageHandlerInterceptorTest {

    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final UUID USER_ID = randomUUID();
    private static final User USER = new User(USER_ID, ORGANIZATION_ID, "", "", "", false, EnumSet.of(Role.ORG_USER));

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

    private EmailAddressValidator emailAddressValidator;
    private UsersUniqueEmailValidator usersUniqueEmailValidator;

    private CommandValidatingMessageHandlerInterceptor commandValidatingMessageHandlerInterceptor;

    @BeforeEach
    void setUp() {
        Mockito.<CommandMessage<?>>when(unitOfWork.getMessage()).thenReturn(commandMessage);
        lenient().when(javaBeanValidator.validate(any())).thenReturn(emptySet());
        emailAddressValidator = new EmailAddressValidator();
        usersUniqueEmailValidator = new UsersUniqueEmailValidator(usersReadService);
        commandValidatingMessageHandlerInterceptor = new CommandValidatingMessageHandlerInterceptor(
                List.of(emailAddressValidator,
                        usersUniqueEmailValidator),
                javaBeanValidator);
    }

//    @Test
//    void willValidateCommandWithSuperSuperInterfaces() throws Exception {
//        Mockito.<Class<?>>when(commandMessage.getPayloadType()).thenReturn(AssignOrgExpertForAssetCommand.class);
//        Mockito.<Object>when(commandMessage.getPayload()).thenReturn(assignOrgExpertForAssetCommand);
//        when(assignOrgExpertForAssetCommand.getUserIds()).thenReturn(Set.of(USER_ID));
//        when(assignOrgExpertForAssetCommand.getExpertId()).thenReturn(USER_ID);
//        when(assignOrgExpertForAssetCommand.getOrganizationId()).thenReturn(ORGANIZATION_ID);
//        when(assignOrgExpertForAssetCommand.getAssetId()).thenReturn(ASSET_ID);
//
//        when(usersReadService.getById(USER_ID)).thenReturn(USER);
//        when(organizationsReadService.getById(ORGANIZATION_ID)).thenReturn(ORGANIZATION);
//        when(assetsReadService.getById(ASSET_ID)).thenReturn(ASSET);
//        when(assetsReadService.exists(ASSET_ID)).thenReturn(true);
//
//        commandValidatingMessageHandlerInterceptor.handle(unitOfWork, interceptorChain);
//        verify(assignOrgExpertForAssetCommand, times(2)).getUserIds();
//        verify(assignOrgExpertForAssetCommand, times(5)).getOrganizationId();
//        verify(assignOrgExpertForAssetCommand, times(2)).getExpertId();
//        verify(assignOrgExpertForAssetCommand, times(4)).getAssetId();
//    }
//
//    @Test
//    void willValidateSuperInterfaceFirst() throws Exception {
//        Mockito.<Class<?>>when(commandMessage.getPayloadType()).thenReturn(AssignOrgExpertForAssetCommand.class);
//        Mockito.<Object>when(commandMessage.getPayload()).thenReturn(assignOrgExpertForAssetCommand);
//        lenient().when(assignOrgExpertForAssetCommand.getUserIds()).thenReturn(Set.of(USER_ID));
//        lenient().when(assignOrgExpertForAssetCommand.getExpertId()).thenReturn(USER_ID);
//        lenient().when(assignOrgExpertForAssetCommand.getOrganizationId()).thenReturn(ORGANIZATION_ID);
//
//        lenient().when(usersReadService.getById(USER_ID)).thenReturn(USER);
//        lenient().when(assetsReadService.exists(ASSET_ID)).thenReturn(true);
//        when(organizationsReadService.getById(ORGANIZATION_ID)).thenThrow(NoSuchElementException.class);
//
//        var exception = assertThrows(IllegalStateException.class,
//                () -> commandValidatingMessageHandlerInterceptor.handle(unitOfWork, interceptorChain));
//
//        assertEquals(String.format("Organization %s does not exist", ORGANIZATION_ID),
//                exception.getMessage());
//    }
//
//    @Test
//    void willValidateCommandWithMultipleInterfaces() throws Exception {
//        var createUserCommand = mock(CreateUserCommand.class);
//        when(createUserCommand.getOrganizationId()).thenReturn(ORGANIZATION_ID);
//        Mockito.<Class<?>>when(commandMessage.getPayloadType()).thenReturn(CreateUserCommand.class);
//        Mockito.<Object>when(commandMessage.getPayload()).thenReturn(createUserCommand);
//        when(organizationsReadService.getById(ORGANIZATION_ID)).thenReturn(ORGANIZATION);
//
//        commandValidatingMessageHandlerInterceptor.handle(unitOfWork, interceptorChain);
//        verify(createUserCommand, times(2)).getEmailAddress();
//        verify(createUserCommand, times(2)).getOrganizationId();
//    }
//
//    @Test
//    void willThrow_WhenJavaBeanValidatorFails() throws Exception {
//        when(javaBeanValidator.validate(any())).thenReturn(Set.of(mock(ConstraintViolation.class)));
//        Mockito.<Class<?>>when(commandMessage.getPayloadType()).thenReturn(AssignOrgExpertForAssetCommand.class);
//        Mockito.<Object>when(commandMessage.getPayload()).thenReturn(assignOrgExpertForAssetCommand);
//
//        assertThrows(ConstraintViolationException.class,
//                () -> commandValidatingMessageHandlerInterceptor.handle(unitOfWork, interceptorChain));
//    }

}
