package com.tenantforge.app.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tenantforge.app.domain.Project;
import com.tenantforge.app.service.ProjectService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(controllers = ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerValidationTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    ProjectService service;

    @MockBean
    com.tenantforge.app.security.JwtAuthenticationFilter jwtFilter;

    @Test
    void create_missingName_returns400() throws Exception {
        String body = "{\"description\":\"d\"}";
        mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void create_valid_returns200() throws Exception {
        when(service.create("P","d")).thenReturn(new Project(UUID.randomUUID(), "P", "d"));
        String body = "{\"name\":\"P\",\"description\":\"d\"}";
        var resp = mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        assertThat(resp.getResponse().getContentAsString()).contains("\"name\":\"P\"");
    }
}
