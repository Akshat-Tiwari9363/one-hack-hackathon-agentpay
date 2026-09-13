package com.agentpay;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class Http402ChallengeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/services/{id}/resource returns HTTP 402 Payment Required with structured quote")
    void testHttp402PaymentRequiredChallenge() throws Exception {
        mockMvc.perform(get("/api/services/svc-trans-a/resource"))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.paymentRequired").value(true))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.amount").value(300.0))
                .andExpect(jsonPath("$.amountPaise").value(30000))
                .andExpect(jsonPath("$.serviceId").value("svc-trans-a"))
                .andExpect(jsonPath("$.providerId").value("prov-a"))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }
}
