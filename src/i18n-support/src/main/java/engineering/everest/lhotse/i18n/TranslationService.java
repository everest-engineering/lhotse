package engineering.everest.lhotse.i18n;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

import static java.lang.Thread.currentThread;

@Slf4j
public class TranslationService {
    private static final TranslationService INSTANCE = new TranslationService();

    private final ResourceBundleMessageSource resourceBundleMessageSource;

    public TranslationService() {
        this.resourceBundleMessageSource = new ResourceBundleMessageSource();
        this.resourceBundleMessageSource.addBasenames("messages");
        this.resourceBundleMessageSource.setBundleClassLoader(currentThread().getContextClassLoader());
    }

    public static TranslationService getInstance() {
        return INSTANCE;
    }

    public String translate(Locale locale, String key, Object... args) {
        try {
            return resourceBundleMessageSource.getMessage(key, args, locale);
        } catch (NoSuchMessageException e) {
            LOGGER.error("Unmapped message key {}", key);
            return key;
        }
    }
}
