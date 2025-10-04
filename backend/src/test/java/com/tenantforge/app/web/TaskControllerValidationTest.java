package com.tenantforge.app.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tenantforge.app.domain.Task;
import com.tenantforge.app.service.TaskService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(controllers = TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerValidationTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    TaskService service;

    @MockBean
    com.tenantforge.app.security.JwtAuthenticationFilter jwtFilter;

    @Test
    void create_missingProjectId_returns400() throws Exception {
        String body = "{\"name\":\"T\"}";
        mvc.perform(MockMvcRequestBuilders.post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void create_valid_returns200() throws Exception {
        UUID projectId = UUID.randomUUID();
        when(service.create(projectId, "T")).thenReturn(new Task(UUID.randomUUID(), projectId, "T"));
        String body = String.format("{\"projectId\":\"%s\",\"name\":\"T\"}", projectId);
        var resp = mvc.perform(MockMvcRequestBuilders.post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        assertThat(resp.getResponse().getContentAsString()).contains("\"name\":\"T\"");
    }
}
