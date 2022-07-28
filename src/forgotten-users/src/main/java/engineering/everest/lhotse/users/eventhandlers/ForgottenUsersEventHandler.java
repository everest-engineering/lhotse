package engineering.everest.lhotse.users.eventhandlers;

import engineering.everest.axon.cryptoshredding.CryptoShreddingKeyService;
import engineering.everest.axon.cryptoshredding.TypeDifferentiatedSecretKeyId;
import engineering.everest.lhotse.axon.replay.ReplayCompletionAware;
import engineering.everest.lhotse.users.domain.events.UserDeletedAndForgottenEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ForgottenUsersEventHandler implements ReplayCompletionAware {

    private final CryptoShreddingKeyService cryptoShreddingKeyService;

    @Autowired
    public ForgottenUsersEventHandler(CryptoShreddingKeyService cryptoShreddingKeyService) {
        this.cryptoShreddingKeyService = cryptoShreddingKeyService;
    }

    @EventHandler
    void on(UserDeletedAndForgottenEvent event) {
        LOGGER.info("Deleting encryption key for forgotten user {}", event.getDeletedUserId());
        cryptoShreddingKeyService.deleteSecretKey(
            new TypeDifferentiatedSecretKeyId(event.getDeletedUserId().toString(), ""));
    }
}
