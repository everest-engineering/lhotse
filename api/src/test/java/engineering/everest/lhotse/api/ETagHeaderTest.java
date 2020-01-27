package engineering.everest.lhotse.api;

import engineering.everest.lhotse.api.config.ETagFilterConfig;
import engineering.everest.lhotse.api.config.TestApiConfig;
import engineering.everest.lhotse.api.rest.controllers.VersionController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {TestApiConfig.class, VersionController.class, ETagFilterConfig.class})
@AutoConfigureMockMvc
@ExtendWith({MockitoExtension.class, SpringExtension.class})
class ETagHeaderTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BuildProperties buildProperties;

    @BeforeEach
    void setUp() {
        when(buildProperties.getVersion()).thenReturn("version");
    }

    @Test
    void eTagWillBeIncludedInResponseHeaders() throws Exception {
        mockMvc.perform(get("/api/version").contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists("ETag"));
    }
}
