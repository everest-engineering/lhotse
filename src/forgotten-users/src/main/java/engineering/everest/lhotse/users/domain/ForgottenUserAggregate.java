package engineering.everest.lhotse.users.domain;

import engineering.everest.lhotse.users.domain.commands.DeleteAndForgetUserCommand;
import engineering.everest.lhotse.users.domain.events.UserDeletedAndForgottenEvent;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;

import java.io.Serializable;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;
import static org.axonframework.modelling.command.AggregateLifecycle.markDeleted;

@NoArgsConstructor
public class ForgottenUserAggregate implements Serializable {

    @AggregateIdentifier
    private UUID userId;

    @CommandHandler
    ForgottenUserAggregate(DeleteAndForgetUserCommand command) {
        apply(new UserDeletedAndForgottenEvent(command.getUserIdToDelete(), command.getRequestingUserId(),
            command.getRequestReason()));
    }

    @EventSourcingHandler
    void on(UserDeletedAndForgottenEvent event) {
        userId = event.getDeletedUserId();
        markDeleted();
    }
}
