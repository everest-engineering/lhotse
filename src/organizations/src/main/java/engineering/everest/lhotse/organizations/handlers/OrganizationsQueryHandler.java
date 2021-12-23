package engineering.everest.lhotse.organizations.handlers;

import engineering.everest.lhotse.organizations.Organization;
import engineering.everest.lhotse.organizations.domain.queries.OrganizationQuery;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrganizationsQueryHandler {

    private final OrganizationsReadService organizationsReadService;

    public OrganizationsQueryHandler(OrganizationsReadService organizationsReadService) {
        this.organizationsReadService = organizationsReadService;
    }

    @QueryHandler
    public Organization handle(OrganizationQuery query) {
        return organizationsReadService.getById(query.getOrganizationId());
    }
}
