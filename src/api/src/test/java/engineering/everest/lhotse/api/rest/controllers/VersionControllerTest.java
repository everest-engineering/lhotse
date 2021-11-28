package engineering.everest.lhotse.api.rest.controllers;

import engineering.everest.lhotse.api.config.TestApiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Properties;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { VersionController.class })
@ContextConfiguration(
    classes = { TestApiConfig.class, VersionController.class, VersionControllerTest.VersionControllerTestConfiguration.class })
class VersionControllerTest {

    private static final String QUOTED_BUILD_TIME_VERSION_STRING = "'build time version string'";
    private static final String UNQUOTED_BUILD_TIME_VERSION_STRING = "build time version string";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void willExposeBuildTimeApplicationVersion() throws Exception {
        mockMvc.perform(get("/api/version"))
            .andExpect(status().isOk())
            .andExpect(content().string(UNQUOTED_BUILD_TIME_VERSION_STRING));
    }

    @TestConfiguration
    static class VersionControllerTestConfiguration {

        @Bean
        public BuildProperties buildProperties() {
            Properties entries = new Properties();
            entries.setProperty("version", QUOTED_BUILD_TIME_VERSION_STRING);
            return new BuildProperties(entries);
        }
    }
}
