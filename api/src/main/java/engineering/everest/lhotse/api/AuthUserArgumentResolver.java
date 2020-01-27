package engineering.everest.lhotse.api;

import engineering.everest.lhotse.axon.common.domain.User;
import engineering.everest.lhotse.axon.security.AuthenticationContextProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthenticationContextProvider authenticationContextProvider;

    @Autowired
    public AuthUserArgumentResolver(AuthenticationContextProvider authenticationContextProvider) {
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == User.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        return authenticationContextProvider.getUser();
    }
}
