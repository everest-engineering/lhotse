package engineering.everest.lhotse.users.domain;

import engineering.everest.lhotse.users.domain.commands.DeleteAndForgetUserCommand;
import engineering.everest.lhotse.users.domain.events.UserDeletedAndForgottenEvent;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static java.util.UUID.randomUUID;

@ExtendWith(MockitoExtension.class)
class ForgottenUserAggregateTest {

    private static final UUID USER_ID = randomUUID();
    private static final UUID ADMIN_ID = randomUUID();
    private static final String REQUEST_REASON = "It's the right thing to do";

    private static final UserDeletedAndForgottenEvent USER_DELETED_AND_FORGOTTEN_EVENT =
        new UserDeletedAndForgottenEvent(USER_ID, ADMIN_ID, REQUEST_REASON);

    private FixtureConfiguration<ForgottenUserAggregate> testFixture;

    @BeforeEach
    void setUp() {
        testFixture = new AggregateTestFixture<>(ForgottenUserAggregate.class);
    }

    @Test
    void emitsUserDeletedAndForgottenEvent_WhenUserIsDeletedAndForgotten() {
        testFixture.givenNoPriorActivity()
            .when(new DeleteAndForgetUserCommand(USER_ID, ADMIN_ID, REQUEST_REASON))
            .expectEvents(USER_DELETED_AND_FORGOTTEN_EVENT);
    }
}
