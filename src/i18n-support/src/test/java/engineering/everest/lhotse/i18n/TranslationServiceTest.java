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
        assertEquals("Malformed email address",
            TranslationService.getInstance().translate(new Locale("smurfs"), "EMAIL_ADDRESS_MALFORMED"));
    }

    @Test
    void willTranslateMessagesWithNoArguments_WhenLocaleUsesNonDefaultTranslation() {
        assertEquals("Fehlerhafte E-Mail-Adresse",
            TranslationService.getInstance().translate(GERMANY, "EMAIL_ADDRESS_MALFORMED"));
    }

    @Test
    void willTranslateMessagesWithArguments_WhenLocaleUsesDefaultTranslation() {
        var australianEnglish = new Locale("en", "AU");
        assertEquals("File file-id does not exist",
            TranslationService.getInstance().translate(australianEnglish, "FILE_DOES_NOT_EXIST", "file-id"));
    }

    @Test
    void willTranslateMessagesWithArguments_WhenLocaleUsesNonDefaultTranslation() {
        assertEquals("Datei file-id existiert nicht",
            TranslationService.getInstance().translate(GERMANY, "FILE_DOES_NOT_EXIST", "file-id"));
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
