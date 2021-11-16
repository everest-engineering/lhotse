package engineering.everest.lhotse.i18n.exceptions;

import engineering.everest.lhotse.i18n.TranslationService;
import org.springframework.context.NoSuchMessageException;

import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

public class TranslatableException extends RuntimeException {

    private final String i18nMessageKey;
    private final Object[] args;

    public TranslatableException(String i18nMessageKey) {
        super(i18nMessageKey);
        this.i18nMessageKey = i18nMessageKey;
        this.args = new Object[0];
    }

    public TranslatableException(String i18nMessageKey, Object... args) {
        super(i18nMessageKey);
        this.i18nMessageKey = i18nMessageKey;
        this.args = args.clone();
    }

    public TranslatableException(String i18nMessageKey, Throwable cause) {
        super(i18nMessageKey, cause);
        this.i18nMessageKey = i18nMessageKey;
        this.args = new Object[0];
    }

    public TranslatableException(String i18nMessageKey, Throwable cause, Object... args) {
        super(i18nMessageKey, cause);
        this.i18nMessageKey = i18nMessageKey;
        this.args = args.clone();
    }

    @Override
    public String getLocalizedMessage() {
        try {
            return TranslationService.getInstance().translate(getLocale(), i18nMessageKey, args);
        } catch (NoSuchMessageException ignored) {
            return getMessage();
        }
    }
}
