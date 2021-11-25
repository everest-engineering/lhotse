package engineering.everest.lhotse.api;

import engineering.everest.lhotse.api.config.ETagFilterConfig;
import engineering.everest.lhotse.api.rest.controllers.VersionController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { VersionController.class })
@ContextConfiguration(classes = { VersionController.class, ETagFilterConfig.class })
class ETagHeaderTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BuildProperties buildProperties;

    @Test
    @WithMockUser
    void eTagWillBeIncludedInResponseHeaders() throws Exception {
        when(buildProperties.getVersion()).thenReturn("version");

        mockMvc.perform(get("/api/version").contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists("ETag"));
    }
}
