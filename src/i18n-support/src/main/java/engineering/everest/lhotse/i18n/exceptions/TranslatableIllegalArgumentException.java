package engineering.everest.lhotse.i18n.exceptions;

public class TranslatableIllegalArgumentException extends TranslatableException {

    public TranslatableIllegalArgumentException(String i18nMessageKey) {
        super(i18nMessageKey);
    }

    public TranslatableIllegalArgumentException(String i18nMessageKey, Object... args) {
        super(i18nMessageKey, args);
    }

    public TranslatableIllegalArgumentException(String i18nMessageKey, Throwable cause) {
        super(i18nMessageKey, cause);
    }

    public TranslatableIllegalArgumentException(String i18nMessageKey, Throwable cause, Object... args) {
        super(i18nMessageKey, cause, args);
    }
}
