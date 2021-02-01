package engineering.everest.lhotse.i18n;

import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

import static org.springframework.util.StringUtils.parseLocaleString;

public class RequestParameterAcceptHeaderLocaleResolver extends AcceptHeaderLocaleResolver {

    public RequestParameterAcceptHeaderLocaleResolver() {
        super();
        super.setDefaultLocale(Locale.getDefault());
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String locale = request.getParameter("locale");
        return locale == null
                ? super.resolveLocale(request)
                : parseLocaleString(locale);
    }
}
