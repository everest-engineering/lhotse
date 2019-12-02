package engineering.everest.starterkit.api.rest.controllers;

import engineering.everest.starterkit.api.config.TestApiConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Properties;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {TestApiConfig.class, VersionController.class, VersionControllerTest.VersionControllerTestConfiguration.class})
@AutoConfigureMockMvc
@ExtendWith({MockitoExtension.class, SpringExtension.class})
class VersionControllerTest {

    private static final String QUOTED_BUILD_TIME_VERSION_STRING = "'build time version string'";
    private static final String UNQUOTED_BUILD_TIME_VERSION_STRING = "build time version string";

    @Autowired
    private MockMvc mockMvc;

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
