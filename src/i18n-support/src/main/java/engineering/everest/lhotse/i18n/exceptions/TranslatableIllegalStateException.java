package engineering.everest.lhotse.i18n.exceptions;

public class TranslatableIllegalStateException extends TranslatableException {
    
    public TranslatableIllegalStateException(String i18nMessageKey) {
        super(i18nMessageKey);
    }

    public TranslatableIllegalStateException(String i18nMessageKey, Object... args) {
        super(i18nMessageKey, args);
    }

    public TranslatableIllegalStateException(String i18nMessageKey, Throwable cause) {
        super(i18nMessageKey, cause);
    }

    public TranslatableIllegalStateException(String i18nMessageKey, Throwable cause, Object... args) {
        super(i18nMessageKey, cause, args);
    }
}
