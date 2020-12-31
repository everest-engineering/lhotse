package engineering.everest.lhotse.i18n;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TranslationServiceTest {

    @BeforeEach
    void setUp() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void willTranslateMessagesWithNoArguments_WhenLocaleUsesDefaultTranslation() {
        assertEquals("Email address already exists",
                TranslationService.getInstance().translate("EMAIL_ADDRESS_ALREADY_EXISTS"));
    }

    @Test
    void willTranslateMessagesWithNoArguments_WhenLocaleUsesNonDefaultTranslation() {
        LocaleContextHolder.setLocale(Locale.GERMANY);
        assertEquals("Diese E-Mail Adresse ist bereits vergeben",
                TranslationService.getInstance().translate("EMAIL_ADDRESS_ALREADY_EXISTS"));
    }

    @Test
    void willTranslateMessagesWithSingleArgument_WhenLocaleUsesDefaultTranslation() {
        assertEquals("Organization org-name is already enabled",
                TranslationService.getInstance().translate("ORGANIZATION_ALREADY_ENABLED", "org-name"));
    }

    @Test
    void willTranslateMessagesWithSingleArgument_WhenLocaleUsesNonDefaultTranslation() {
        LocaleContextHolder.setLocale(Locale.GERMANY);
        assertEquals("Organisation org-name ist bereits aktiviert",
                TranslationService.getInstance().translate("ORGANIZATION_ALREADY_ENABLED", "org-name"));
    }
}