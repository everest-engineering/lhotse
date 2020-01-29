package engineering.everest.lhotse.api.rest.annotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZATION_ADMIN') and #requestingUser.organizationId == #organizationId)")
public @interface AdminOrAdminOfTargetOrganization {
}
