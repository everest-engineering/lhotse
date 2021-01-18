package engineering.everest.lhotse.i18n;


import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

import static java.lang.Thread.currentThread;

public class TranslationService {
    private static final TranslationService INSTANCE = new TranslationService();

    private final ResourceBundleMessageSource resourceBundleMessageSource;

    public TranslationService() {
        this.resourceBundleMessageSource = new ResourceBundleMessageSource();
        this.resourceBundleMessageSource.addBasenames("messages");
        this.resourceBundleMessageSource.setBundleClassLoader(currentThread().getContextClassLoader());
    }

    public String translate(Locale locale, String key, Object... args) {
        return resourceBundleMessageSource.getMessage(key, args, locale);
    }

    public static TranslationService getInstance() {
        return INSTANCE;
    }
}
