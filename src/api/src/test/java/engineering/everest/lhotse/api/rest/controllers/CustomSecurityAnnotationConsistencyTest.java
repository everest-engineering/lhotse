package engineering.everest.lhotse.api.rest.controllers;

import engineering.everest.lhotse.api.rest.annotations.AdminOnly;
import engineering.everest.lhotse.api.rest.annotations.AdminOrRegisteredUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.core.parameters.DefaultSecurityParameterNameDiscoverer;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

class CustomSecurityAnnotationConsistencyTest {

    private static final String PRINCIPAL = "principal";

    private final DefaultSecurityParameterNameDiscoverer parameterNameDiscoverer = new DefaultSecurityParameterNameDiscoverer();

    @Test
    void allControllerHandlerMethodsHaveMatchingAnnotationAndSignature() {
        for (Class<?> controllerClass : scanForControllerClasses()) {
            controllerClassHasMatchingAnnotationAndSignature(controllerClass);
        }
    }

    private List<Class<?>> scanForControllerClasses() {
        var classPathScanningCandidateComponentProvider = new ClassPathScanningCandidateComponentProvider(false);
        classPathScanningCandidateComponentProvider.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        return classPathScanningCandidateComponentProvider.findCandidateComponents(this.getClass().getPackageName()).stream()
            .map(BeanDefinition::getBeanClassName).map((String className) -> {
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).collect(toList());
    }

    private void controllerClassHasMatchingAnnotationAndSignature(Class<?> controllerClass) {
        List<Method> methods = Arrays.stream(controllerClass.getMethods())
            .filter(this::isSecurityAnnotationPresent)
            .collect(toList());
        for (Method method : methods) {
            if (!isMethodSignatureConsistentWithSecurityAnnotation(method)) {
                throw new AssertionError(String.format("%s.%s has inconsistent usage of custom security annotation and method signature",
                    controllerClass, method.getName()));
            }
        }
    }

    private boolean isSecurityAnnotationPresent(Method method) {
        return method.isAnnotationPresent(AdminOrRegisteredUser.class)
            || method.isAnnotationPresent(AdminOnly.class);
    }

    private boolean isMethodSignatureConsistentWithSecurityAnnotation(Method method) {
        Parameter[] parameters = method.getParameters();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        return isPrincipalParameterPresent(parameters, parameterNames);
    }

    private boolean isPrincipalParameterPresent(Parameter[] parameters, String[] parameterNames) {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType().equals(Principal.class) && parameterNames[i].equals(PRINCIPAL)) {
                return true;
            }
        }
        return false;
    }
}
