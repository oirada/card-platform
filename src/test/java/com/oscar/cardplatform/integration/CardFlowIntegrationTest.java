package com.oscar.cardplatform.integration;

import com.oscar.cardplatform.domain.entity.Transaction;
import com.oscar.cardplatform.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
public class CardFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void fullFlow_create_enrol_tx_annul_and_errors() throws Exception {
        // 1) Create card
        String createJson = """
                {
                  "pan": "4111111111111112",
                  "titular": "Test User",
                  "cedula": "1712345678",
                  "tipo": "Debito",
                  "telefono": "0987654321"
                }""";

        String createResp = mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract identificador and numeroValidacion using regex
        String identificador = extractJsonValue(createResp, "identificador");
        int numeroValidacion = Integer.parseInt(extractJsonValue(createResp, "numeroValidacion"));

        assertThat(identificador).isNotBlank();
        assertThat(numeroValidacion).isGreaterThan(0).isLessThanOrEqualTo(100);

        // 2) Enrol card
        String enrolJson = String.format("{\"identificador\": \"%s\", \"numeroValidacion\": %d}", identificador, numeroValidacion);
        mockMvc.perform(post("/api/cards/enrol")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(enrolJson))
                .andExpect(status().isOk());

        // 3) Create transaction
        String ref = "123456";
        String txJson = String.format("{\"identificador\": \"%s\", \"referencia\": \"%s\", \"total\": 100.5, \"direccion\": \"Av Test\"}", identificador, ref);
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(txJson))
                .andExpect(status().isOk());

        // 4) Creating same reference should fail with CONFLICT
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(txJson))
                .andExpect(status().isConflict());

        // 5) Annul transaction (happy path)
        String annulJson = String.format("{\"identificador\": \"%s\", \"referencia\": \"%s\", \"total\": 100.5}", identificador, ref);
        mockMvc.perform(post("/api/transactions/annul")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(annulJson))
                .andExpect(status().isOk());

        // 6) Create another tx and set its createdAt older than 6 minutes to test annul failure
        String ref2 = "654321";
        String tx2Json = String.format("{\"identificador\": \"%s\", \"referencia\": \"%s\", \"total\": 50.0, \"direccion\": \"Av Test\"}", identificador, ref2);
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tx2Json))
                .andExpect(status().isOk());

        // set createdAt back in DB to simulate old transaction
        Transaction tx2 = transactionRepository.findByReference(ref2).orElseThrow();
        tx2 = Transaction.builder()
                .id(tx2.getId())
                .card(tx2.getCard())
                .amount(tx2.getAmount())
                .description(tx2.getDescription())
                .reference(tx2.getReference())
                .address(tx2.getAddress())
                .status(tx2.getStatus())
                .createdAt(LocalDateTime.now().minusMinutes(6))
                .build();
        transactionRepository.save(tx2);

        // attempt annul should return CONFLICT (cannot annul - older than 5 minutes)
        String annul2Json = String.format("{\"identificador\": \"%s\", \"referencia\": \"%s\", \"total\": 50.0}", identificador, ref2);
        mockMvc.perform(post("/api/transactions/annul")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(annul2Json))
                .andExpect(status().isConflict());
    }

    /**
     * Helper method to extract JSON field value using regex.
     * Searches for "fieldName":"value" (strings) or "fieldName":value (numbers) and returns the value.
     */
    private String extractJsonValue(String json, String fieldName) {
        // Try to match string values first: "fieldName":"value"
        Pattern patternString = Pattern.compile("\"" + fieldName + "\":\"([^\"]+)\"");
        Matcher matcherString = patternString.matcher(json);
        if (matcherString.find()) {
            return matcherString.group(1);
        }

        // Try to match numeric values: "fieldName":123
        Pattern patternNumber = Pattern.compile("\"" + fieldName + "\":(\\d+)");
        Matcher matcherNumber = patternNumber.matcher(json);
        if (matcherNumber.find()) {
            return matcherNumber.group(1);
        }

        throw new IllegalArgumentException("Field " + fieldName + " not found in JSON response");
    }
}
