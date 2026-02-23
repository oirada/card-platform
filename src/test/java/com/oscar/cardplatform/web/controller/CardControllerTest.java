package com.oscar.cardplatform.web.controller;

import com.oscar.cardplatform.web.dto.CreateCardRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateCardEndpoint() throws Exception {
        String json = "{\n" +
                "  \"pan\": \"4111111111112222\",\n" +
                "  \"titular\": \"Juan Perez\",\n" +
                "  \"cedula\": \"1712345678\",\n" +
                "  \"tipo\": \"Debito\",\n" +
                "  \"telefono\": \"0987654321\"\n" +
                "}";

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }
}