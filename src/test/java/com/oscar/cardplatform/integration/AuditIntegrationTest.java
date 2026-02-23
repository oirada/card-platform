package com.oscar.cardplatform.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oscar.cardplatform.repository.AuditEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
public class AuditIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditEventRepository auditEventRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createCard_generatesAuditEvent_andCanBeQueried() throws Exception {
        String createJson = "{\"pan\":\"4111111111111111\",\"titular\":\"Test User\",\"cedula\":\"1234567890\",\"tipo\":\"Credito\",\"telefono\":\"0987654321\"}";

        MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        String content = createResult.getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(content);
        assertThat(root.has("identificador")).isTrue();

        String id = root.path("identificador").asText(null);
        assertThat(id).isNotNull().isNotEmpty();

        // now query audit endpoint
        MvcResult auditResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/audit/by-entity/" + id))
                .andExpect(status().isOk())
                .andReturn();

        String auditContent = auditResult.getResponse().getContentAsString();
        // first try: ensure repository has the event (direct DB check)
        assertThat(auditEventRepository.findByEntityId(id)).isNotEmpty();

        assertThat(auditContent).isNotEmpty();
        // should contain at least one event for entityId
        assertThat(auditContent).contains(id);
    }
}
