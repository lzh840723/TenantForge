package com.tenantforge.app.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tenantforge.app.domain.TimeEntry;
import com.tenantforge.app.service.TimeEntryService;
import java.time.Instant;
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

@WebMvcTest(controllers = TimeEntryController.class)
@AutoConfigureMockMvc(addFilters = false)
class TimeEntryControllerValidationTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    TimeEntryService service;

    @MockBean
    com.tenantforge.app.security.JwtAuthenticationFilter jwtFilter;

    @Test
    void create_missingFields_returns400() throws Exception {
        String body = "{\"notes\":\"n\"}";
        mvc.perform(MockMvcRequestBuilders.post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void create_invalidTime_returns400() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String s = "2024-01-02T10:00:00Z";
        String e = "2024-01-02T09:00:00Z";
        when(service.create(taskId, userId, Instant.parse(s), Instant.parse(e), "n"))
                .thenThrow(new IllegalArgumentException("endedAt must be after startedAt"));
        String body = String.format("{\"taskId\":\"%s\",\"userId\":\"%s\",\"startedAt\":\"%s\",\"endedAt\":\"%s\",\"notes\":\"n\"}",
                taskId, userId, s, e);
        mvc.perform(MockMvcRequestBuilders.post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void create_valid_returns200() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant s = Instant.parse("2024-01-02T10:00:00Z");
        Instant e = Instant.parse("2024-01-02T11:00:00Z");
        when(service.create(taskId, userId, s, e, "n")).thenReturn(new TimeEntry(UUID.randomUUID(), taskId, userId, s, e, "n"));
        String body = String.format("{\"taskId\":\"%s\",\"userId\":\"%s\",\"startedAt\":\"%s\",\"endedAt\":\"%s\",\"notes\":\"n\"}",
                taskId, userId, s, e);
        var resp = mvc.perform(MockMvcRequestBuilders.post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        assertThat(resp.getResponse().getContentAsString()).contains("\"notes\":\"n\"");
    }
}
