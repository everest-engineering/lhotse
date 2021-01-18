package engineering.everest.lhotse.i18n;


import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TranslationServiceTest {

    @Test
    void willTranslateMessagesWithNoArguments_WhenLocaleUsesDefaultTranslation() {
        assertEquals("Email address already exists",
                TranslationService.getInstance().translate(new Locale("smurfs"), "EMAIL_ADDRESS_ALREADY_EXISTS"));
    }

    @Test
    void willTranslateMessagesWithNoArguments_WhenLocaleUsesNonDefaultTranslation() {
        assertEquals("Diese E-Mail Adresse ist bereits vergeben",
                TranslationService.getInstance().translate(Locale.GERMANY, "EMAIL_ADDRESS_ALREADY_EXISTS"));
    }

    @Test
    void willTranslateMessagesWithSingleArgument_WhenLocaleUsesDefaultTranslation() {
        Locale australianEnglish = new Locale("en", "AU");
        assertEquals("Organization org-name is already enabled",
                TranslationService.getInstance().translate(australianEnglish, "ORGANIZATION_ALREADY_ENABLED", "org-name"));
    }

    @Test
    void willTranslateMessagesWithSingleArgument_WhenLocaleUsesNonDefaultTranslation() {
        assertEquals("Organisation org-name ist bereits aktiviert",
                TranslationService.getInstance().translate(Locale.GERMANY, "ORGANIZATION_ALREADY_ENABLED", "org-name"));
    }
}