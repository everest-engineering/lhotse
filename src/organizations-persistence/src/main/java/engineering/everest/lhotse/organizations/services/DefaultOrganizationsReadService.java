package engineering.everest.lhotse.organizations.services;

import engineering.everest.lhotse.organizations.Organization;
import engineering.everest.lhotse.organizations.persistence.OrganizationsRepository;
import engineering.everest.lhotse.organizations.persistence.PersistableOrganization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Service
public class DefaultOrganizationsReadService implements OrganizationsReadService {

    private final OrganizationsRepository organizationsRepository;

    @Autowired
    public DefaultOrganizationsReadService(OrganizationsRepository organizationsRepository) {
        this.organizationsRepository = organizationsRepository;
    }

    @Override
    public boolean exists(UUID organizationId) {
        return organizationsRepository.findById(organizationId).isPresent();
    }

    @Override
    public Organization getById(UUID id) {
        return organizationsRepository.findById(id).orElseThrow().toDomain();
    }

    @Override
    public List<Organization> getOrganizations() {
        return organizationsRepository.findAll().stream()
            .map(PersistableOrganization::toDomain)
            .collect(toList());
    }
}
