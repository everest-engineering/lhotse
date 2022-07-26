package engineering.everest.lhotse.api.config;

import engineering.everest.lhotse.common.RandomFieldsGenerator;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FirstTimeUserBootstrappingFilterTest {

    private FirstTimeUserBootstrappingFilter filterConfig;

    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private CommandGateway commandGateway;
    @Mock
    private RandomFieldsGenerator randomFieldsGenerator;
    @Mock
    private UsersReadService usersReadService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filterConfig = new FirstTimeUserBootstrappingFilter("default-client",
            commandGateway, usersReadService, randomFieldsGenerator);
    }

    @Test
    void shouldNotFilterMethod_WillReturnTrueForRootPath() {
        when(httpServletRequest.getServletPath()).thenReturn("/");
        assertTrue(filterConfig.shouldNotFilter(httpServletRequest));
    }

    @Test
    void shouldNotFilterMethod_WillReturnTrueForInvalidPath() {
        when(httpServletRequest.getServletPath()).thenReturn("/invalid");
        assertTrue(filterConfig.shouldNotFilter(httpServletRequest));
    }

    @Test
    void shouldNotFilterMethod_WillReturnTrueForVersionApiPath() {
        when(httpServletRequest.getServletPath()).thenReturn("/api/version");
        assertTrue(filterConfig.shouldNotFilter(httpServletRequest));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "/admin/organizations",
            "/admin/organizations/906e3a66-c6ee-418f-a411-4905eed31fde",
            "/api/organizations/906e3a66-c6ee-418f-a411-4905eed31fde",
            "/api/organizations/906e3a66-c6ee-418f-a411-4905eed31fde/users",
            "/api/user",
            "/api/user/profile-photo",
            "/api/user/profile-photo/thumbnail",
            "/api/users",
            "/api/users/9af4cb20-7eb7-486a-a30b-55141714f6af",
            "/api/users/9af4cb20-7eb7-486a-a30b-55141714f6af/forget",
            "/api/users/9af4cb20-7eb7-486a-a30b-55141714f6af/roles",
        })
    void shouldNotFilterMethod_WillReturnFalseForProtectedApiPaths(String path) {
        when(httpServletRequest.getServletPath()).thenReturn(path);
        assertFalse(filterConfig.shouldNotFilter(httpServletRequest));
    }
}
