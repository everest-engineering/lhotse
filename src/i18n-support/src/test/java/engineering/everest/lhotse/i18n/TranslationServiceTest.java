package engineering.everest.lhotse.i18n;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Locale;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMANY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.org.lidalia.slf4jext.Level.ERROR;

@ExtendWith(TestLoggerFactoryExtension.class)
class TranslationServiceTest {

    private final TestLogger logger = TestLoggerFactory.getTestLogger(TranslationService.class);

    @BeforeEach
    void setUp() {
        TestLoggerFactory.clear();
    }

    @Test
    void willTranslateMessagesWithNoArguments_WhenLocaleUsesDefaultTranslation() {
        assertEquals("Email address already exists",
            TranslationService.getInstance().translate(new Locale("smurfs"), "EMAIL_ADDRESS_ALREADY_EXISTS"));
    }

    @Test
    void willTranslateMessagesWithNoArguments_WhenLocaleUsesNonDefaultTranslation() {
        assertEquals("Diese E-Mail Adresse ist bereits vergeben",
            TranslationService.getInstance().translate(GERMANY, "EMAIL_ADDRESS_ALREADY_EXISTS"));
    }

    @Test
    void willTranslateMessagesWithSingleArgument_WhenLocaleUsesDefaultTranslation() {
        var australianEnglish = new Locale("en", "AU");
        assertEquals("Organization org-name is already enabled",
            TranslationService.getInstance().translate(australianEnglish, "ORGANIZATION_ALREADY_ENABLED", "org-name"));
    }

    @Test
    void willTranslateMessagesWithSingleArgument_WhenLocaleUsesNonDefaultTranslation() {
        assertEquals("Organisation org-name ist bereits aktiviert",
            TranslationService.getInstance().translate(GERMANY, "ORGANIZATION_ALREADY_ENABLED", "org-name"));
    }

    @Test
    void willLogWhenMessageKeyIsInvalid() {
        TranslationService.getInstance().translate(ENGLISH, "unknown-key");

        var loggingEvent = logger.getLoggingEvents().get(0);
        assertEquals(ERROR, loggingEvent.getLevel());
        assertEquals("Unmapped message key {}", loggingEvent.getMessage());
        assertEquals("unknown-key", loggingEvent.getArguments().get(0));
    }

    @Test
    void willReturnMessageKeyWhenKeyIsInvalid() {
        assertEquals("unknown-key",
            TranslationService.getInstance().translate(ENGLISH, "unknown-key"));
    }
}
