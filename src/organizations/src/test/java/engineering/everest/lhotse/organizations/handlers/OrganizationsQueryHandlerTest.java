package engineering.everest.lhotse.organizations.handlers;

import engineering.everest.lhotse.organizations.Organization;
import engineering.everest.lhotse.organizations.domain.queries.OrganizationQuery;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationsQueryHandlerTest {

    private static final UUID ORGANIZATION_ID = randomUUID();

    private OrganizationsQueryHandler organizationsQueryHandler;

    @Mock
    private OrganizationsReadService organizationsReadService;

    @BeforeEach
    void setUp() {
        organizationsQueryHandler = new OrganizationsQueryHandler(organizationsReadService);
    }

    @Test
    void organizationQuery_WillReturnCurrentProjectedOrganizationState() {
        var organization = mock(Organization.class);
        when(organizationsReadService.getById(ORGANIZATION_ID)).thenReturn(organization);

        assertEquals(organization, organizationsQueryHandler.handle(new OrganizationQuery(ORGANIZATION_ID)));
    }
}
