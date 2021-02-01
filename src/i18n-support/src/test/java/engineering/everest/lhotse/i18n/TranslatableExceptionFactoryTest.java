package engineering.everest.lhotse.i18n;

import org.junit.jupiter.api.Test;

import static engineering.everest.lhotse.i18n.MessageKeys.EMAIL_ADDRESS_MALFORMED;
import static engineering.everest.lhotse.i18n.MessageKeys.ORGANIZATION_IS_DISABLED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TranslatableExceptionFactoryTest {

    @Test
    void willThrowSingleKeyArgumentExceptions() {
        try {
            TranslatableExceptionFactory.throwForKey(EMAIL_ADDRESS_MALFORMED);
        } catch (Exception e) {
            assertEquals("Malformed email address", e.getLocalizedMessage());
            assertNull(e.getCause());
        }
    }

    @Test
    void willFail_WhenSingleKeyArgumentNotAValidKey() {
        try {
            TranslatableExceptionFactory.throwForKey("KEY_NOT_VALID");
        } catch (Exception e) {
            assertEquals("Invalid message key for translatable exception KEY_NOT_VALID", e.getLocalizedMessage());
        }
    }

    @Test
    void willThrowKeyArgumentWithCauseExceptions() {
        IllegalCallerException cause = new IllegalCallerException("not really");
        try {
            TranslatableExceptionFactory.throwForKey(EMAIL_ADDRESS_MALFORMED, cause);
        } catch (Exception e) {
            assertEquals("Malformed email address", e.getLocalizedMessage());
            assertEquals(cause, e.getCause());
        }
    }

    @Test
    void willThrowKeyWithVariableArgumentsExceptions() {
        try {
            TranslatableExceptionFactory.throwForKey(ORGANIZATION_IS_DISABLED, "org-id");
        } catch (Exception e) {
            assertEquals("Organization org-id is disabled", e.getLocalizedMessage());
            assertNull(e.getCause());
        }
    }

    @Test
    void willThrowKeyWithCauseAndVariableArgumentsExceptions() {
        IllegalCallerException cause = new IllegalCallerException("not really");

        try {
            TranslatableExceptionFactory.throwForKey(ORGANIZATION_IS_DISABLED, cause, "org-id");
        } catch (Exception e) {
            assertEquals("Organization org-id is disabled", e.getLocalizedMessage());
            assertEquals(cause, e.getCause());
        }
    }
}

