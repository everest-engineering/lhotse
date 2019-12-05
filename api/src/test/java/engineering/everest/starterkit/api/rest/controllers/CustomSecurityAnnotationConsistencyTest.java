package engineering.everest.starterkit.api.rest.controllers;

import engineering.everest.starterkit.api.rest.annotations.AdminOrAdminOfTargetOrganization;
import engineering.everest.starterkit.api.rest.annotations.AdminOrExpertOfTargetOrganization;
import engineering.everest.starterkit.api.rest.annotations.AdminOrUserOfTargetOrganization;
import engineering.everest.starterkit.axon.common.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.core.parameters.DefaultSecurityParameterNameDiscoverer;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

public class CustomSecurityAnnotationConsistencyTest {

    private static final String REQUESTING_USER = "requestingUser";
    private static final String ORGANIZATION_ID = "organizationId";

    private DefaultSecurityParameterNameDiscoverer parameterNameDiscoverer = new DefaultSecurityParameterNameDiscoverer();

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
        return method.isAnnotationPresent(AdminOrAdminOfTargetOrganization.class)
                || method.isAnnotationPresent(AdminOrUserOfTargetOrganization.class)
                || method.isAnnotationPresent(AdminOrExpertOfTargetOrganization.class);
    }

    private boolean isMethodSignatureConsistentWithSecurityAnnotation(Method method) {
        Parameter[] parameters = method.getParameters();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        return isRequestingUserParameterPresent(parameters, parameterNames) && isOrganizationIdParameterPresent(parameters, parameterNames);
    }

    private boolean isRequestingUserParameterPresent(Parameter[] parameters, String[] parameterNames) {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType().equals(User.class) && parameterNames[i].equals(REQUESTING_USER)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOrganizationIdParameterPresent(Parameter[] parameters, String[] parameterNames) {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(PathVariable.class)
                    && parameters[i].getType().equals(UUID.class)
                    && parameterNames[i].equals(ORGANIZATION_ID)) {
                return true;
            }
        }
        return false;
    }

}
