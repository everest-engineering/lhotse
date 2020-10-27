package engineering.everest.lhotse.organizations.services;

import engineering.everest.lhotse.axon.common.services.ReadService;
import engineering.everest.lhotse.organizations.Organization;

import java.util.List;
import java.util.UUID;

public interface OrganizationsReadService extends ReadService<Organization> {

    List<Organization> getOrganizations();

    boolean exists(UUID organizationId);
}
