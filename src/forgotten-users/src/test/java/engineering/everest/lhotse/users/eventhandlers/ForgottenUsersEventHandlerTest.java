package engineering.everest.lhotse.users.eventhandlers;

import engineering.everest.axon.cryptoshredding.CryptoShreddingKeyService;
import engineering.everest.axon.cryptoshredding.TypeDifferentiatedSecretKeyId;
import engineering.everest.lhotse.users.domain.events.UserDeletedAndForgottenEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ForgottenUsersEventHandlerTest {

    private static final UUID USER_ID = randomUUID();
    private static final UUID ADMIN_ID = randomUUID();

    private ForgottenUsersEventHandler forgottenUsersEventHandler;

    @Mock
    private CryptoShreddingKeyService cryptoShreddingKeyService;

    @BeforeEach
    void setUp() {
        forgottenUsersEventHandler = new ForgottenUsersEventHandler(cryptoShreddingKeyService);
    }

    @Test
    void onUserDeletedAndForgottenEvent_WillDiscardSecretKey() {
        forgottenUsersEventHandler.on(new UserDeletedAndForgottenEvent(USER_ID, ADMIN_ID, "GDPR request"));

        verify(cryptoShreddingKeyService).shredSecretKey(new TypeDifferentiatedSecretKeyId(USER_ID.toString(), ""));
    }
}
