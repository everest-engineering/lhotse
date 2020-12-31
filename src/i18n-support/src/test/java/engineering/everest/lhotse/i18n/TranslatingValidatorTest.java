package engineering.everest.lhotse.i18n;

import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalArgumentException;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import static java.util.Locale.ENGLISH;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TranslatingValidatorTest {

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(ENGLISH);
    }

    @Test
    void isTrue_WillThrowTranslatableIllegalArgumentExceptionOnFailure() {
        assertThrows(TranslatableIllegalArgumentException.class, () ->
                TranslatingValidator.isTrue(false, "USER_DISPLAY_NAME_MISSING"));
    }

    @Test
    void isValidState_WillThrowTranslatableIllegalStateExceptionOnFailure() {
        assertThrows(TranslatableIllegalStateException.class, () ->
                TranslatingValidator.isValidState(false, "USER_DISPLAY_NAME_MISSING"));
    }
}