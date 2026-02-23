package com.oscar.cardplatform.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityGenericMessagesTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void enrol_nonexistentCard_returnsGenericMessage_notSpecific() throws Exception {
        // Intentar enrolar con identificador inexistente
        String enrolJson = "{\"identificador\": \"nonexistent-id-12345\", \"numeroValidacion\": 50}";

        String response = mockMvc.perform(post("/api/cards/enrol")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(enrolJson))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        // Verificar que NO expone el mensaje específico "Tarjeta no existe"
        assertThat(response).doesNotContain("Tarjeta no existe");
        // Verificar que contiene el mensaje genérico
        assertThat(response).contains("Operación inválida");
    }

    @Test
    void getCard_nonexistentIdentificador_returnsNotFound() throws Exception {
        // GET con identificador inexistente
        String response = mockMvc.perform(get("/api/cards/nonexistent-id-abc123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        // El response body NO debe exponer "Tarjeta no existe"
        assertThat(response).doesNotContain("Tarjeta no existe");
    }

    @Test
    void deleteCard_nonexistentIdentificador_returnsGenericMessage() throws Exception {
        // DELETE con identificador inexistente
        String response = mockMvc.perform(delete("/api/cards/nonexistent-id-xyz")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        // NO debe revelar que la tarjeta no existe
        assertThat(response).doesNotContain("Tarjeta no existe");
        // Debe contener mensaje genérico
        assertThat(response).contains("Operación inválida");
    }

    @Test
    void createTransaction_nonexistentCard_returnsGenericMessage() throws Exception {
        // POST transacción con identificador de tarjeta inexistente
        String txJson = "{\"identificador\": \"fake-id-999\", \"referencia\": \"111111\", \"total\": 50.0, \"direccion\": \"Av Test\"}";

        String response = mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(txJson))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        // NO debe revelar "Tarjeta no existe"
        assertThat(response).doesNotContain("Tarjeta no existe");
        assertThat(response).contains("Operación inválida");
    }

    @Test
    void annulTransaction_invalidReference_returnsGenericMessage() throws Exception {
        // POST anular con referencia inexistente
        String annulJson = "{\"identificador\": \"some-id\", \"referencia\": \"999999\", \"total\": 50.0}";

        String response = mockMvc.perform(post("/api/transactions/annul")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(annulJson))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        // NO debe revelar detalles específicos sobre referencia
        assertThat(response).doesNotContain("numero de referencia inválido");
        assertThat(response).doesNotContain("Referencia no encontrada");
        assertThat(response).contains("Operación inválida");
    }
}
