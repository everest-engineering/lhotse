package engineering.everest.lhotse.i18n;

import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalArgumentException;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalStateException;

@SuppressWarnings("PMD.ClassNamingConventions")
public class TranslatingValidator {

    public static void isTrue(boolean expression, String i18nMessageKey, Object... args) {
        if (!expression) {
            throw new TranslatableIllegalArgumentException(i18nMessageKey, args);
        }
    }

    public static void isValidState(boolean expression, String i18nMessageKey, Object... args) {
        if (!expression) {
            throw new TranslatableIllegalStateException(i18nMessageKey, args);
        }
    }
}
