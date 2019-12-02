package engineering.everest.starterkit.organizations.services;

import engineering.everest.starterkit.axon.common.services.ReadService;
import engineering.everest.starterkit.organizations.Organization;

import java.util.List;
import java.util.UUID;

public interface OrganizationsReadService extends ReadService<Organization> {

    List<Organization> getOrganizations();

    boolean exists(UUID organizationId);
}
