package engineering.everest.lhotse.i18n;


import org.springframework.context.support.ResourceBundleMessageSource;

import static java.lang.Thread.currentThread;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

public class TranslationService {
    private static final TranslationService INSTANCE = new TranslationService();

    private final ResourceBundleMessageSource resourceBundleMessageSource;

    public TranslationService() {
        this.resourceBundleMessageSource = new ResourceBundleMessageSource();
        this.resourceBundleMessageSource.addBasenames("messages");
        this.resourceBundleMessageSource.setBundleClassLoader(currentThread().getContextClassLoader());
    }

    public String translate(String key, Object... args) {
        return resourceBundleMessageSource.getMessage(key, args, getLocale());
    }

    public static TranslationService getInstance() {
        return INSTANCE;
    }
}
