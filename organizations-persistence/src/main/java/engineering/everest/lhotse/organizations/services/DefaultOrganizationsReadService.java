package engineering.everest.lhotse.organizations.services;

import engineering.everest.lhotse.organizations.persistence.OrganizationsRepository;
import engineering.everest.lhotse.organizations.persistence.PersistableOrganization;
import engineering.everest.lhotse.organizations.Organization;
import engineering.everest.lhotse.organizations.OrganizationAddress;
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
        return convert(organizationsRepository.findById(id).orElseThrow());
    }

    @Override
    public List<Organization> getOrganizations() {
        return organizationsRepository.findAll().stream()
                .map(DefaultOrganizationsReadService::convert)
                .collect(toList());
    }

    private static Organization convert(PersistableOrganization persistableOrganization) {
        var address = persistableOrganization.getAddress();
        var organizationAddress = new OrganizationAddress(address.getStreet(),
                address.getCity(), address.getState(), address.getCountry(), address.getPostalCode());
        return new Organization(persistableOrganization.getId(), persistableOrganization.getOrganizationName(),
                organizationAddress, persistableOrganization.getWebsiteUrl(), persistableOrganization.getContactName(),
                persistableOrganization.getPhoneNumber(), persistableOrganization.getEmailAddress(),
                persistableOrganization.isDeregistered());
    }
}
