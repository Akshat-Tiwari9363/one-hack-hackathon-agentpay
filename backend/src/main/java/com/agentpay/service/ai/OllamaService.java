package com.agentpay.service.ai;

import com.agentpay.domain.ServiceEntity;
import com.agentpay.dto.AutonomousPaymentOutcomeDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OllamaService {

    private static final Logger log = LoggerFactory.getLogger(OllamaService.class);
    private static final Pattern THINK_PATTERN = Pattern.compile("<think>[\\s\\S]*?</think>", Pattern.CASE_INSENSITIVE);
    private static final Pattern JSON_PATTERN = Pattern.compile("\\{[\\s\\S]*?\\}");

    private final String baseUrl;
    private final String model;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class AISelectionResult {
        private final String selectedServiceId;
        private final String providerId;
        private final Long proposedPricePaise;
        private final String reasoning;
        private final String engine;
        private final String modelName;

        public AISelectionResult(String selectedServiceId, String providerId, Long proposedPricePaise, String reasoning, String engine, String modelName) {
            this.selectedServiceId = selectedServiceId;
            this.providerId = providerId;
            this.proposedPricePaise = proposedPricePaise;
            this.reasoning = reasoning;
            this.engine = engine;
            this.modelName = modelName;
        }

        public String getSelectedServiceId() { return selectedServiceId; }
        public String getProviderId() { return providerId; }
        public Long getProposedPricePaise() { return proposedPricePaise; }
        public String getReasoning() { return reasoning; }
        public String getEngine() { return engine; }
        public String getModelName() { return modelName; }
    }

    public static class AIActionDecision {
        private final String action; // "PURCHASE_SERVICE" or "DONE"
        private final String serviceId;
        private final String providerId;
        private final String reason;
        private final String engine; // "Ollama (deepseek-r1:7b)" or "FALLBACK"
        private final String model;

        public AIActionDecision(String action, String serviceId, String providerId, String reason, String engine, String model) {
            this.action = action;
            this.serviceId = serviceId;
            this.providerId = providerId;
            this.reason = reason;
            this.engine = engine;
            this.model = model;
        }

        public String getAction() { return action; }
        public String getServiceId() { return serviceId; }
        public String getProviderId() { return providerId; }
        public String getReason() { return reason; }
        public String getEngine() { return engine; }
        public String getModel() { return model; }
        public boolean isDone() { return "DONE".equalsIgnoreCase(action); }
    }

    public OllamaService(
            @Value("${app.ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${app.ollama.model:deepseek-r1:7b}") String model,
            RestTemplateBuilder builder) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(45))
                .build();
    }

    public boolean isAvailable() {
        try {
            ResponseEntity<String> res = restTemplate.getForEntity(baseUrl + "/api/tags", String.class);
            return res.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    public String getEngineName() {
        return isAvailable() ? "Ollama" : "FALLBACK";
    }

    public String getModelName() {
        return model;
    }

    /**
     * Autonomous decision-making for the multi-step agent loop.
     * Evaluates task, candidate services, remaining tokens, and previous action outcomes.
     */
    public AIActionDecision decideNextAction(
            String userTask,
            List<ServiceEntity> candidates,
            Long remainingTokens,
            List<AutonomousPaymentOutcomeDto> pastActions,
            boolean adversarial) {

        if (candidates == null || candidates.isEmpty()) {
            return new AIActionDecision("DONE", null, null, "No services available in catalog.", getEngineName(), model);
        }

        long remaining = remainingTokens != null ? remainingTokens : 1000L;

        // Check if all affordable services have already been purchased
        Set<String> alreadyPurchasedAndDelivered = new HashSet<>();
        Set<String> deniedServices = new HashSet<>();
        for (AutonomousPaymentOutcomeDto past : pastActions) {
            if ("DELIVERED".equalsIgnoreCase(past.getServiceStatus())) {
                alreadyPurchasedAndDelivered.add(past.getServiceId());
            } else if ("DENIED".equalsIgnoreCase(past.getBlockchainStatus())) {
                deniedServices.add(past.getServiceId());
            }
        }

        // Adversarial prompt injection mode: explicitly attempt to breach the spending limit
        if (adversarial && deniedServices.isEmpty()) {
            ServiceEntity expensive = candidates.stream()
                    .filter(s -> !alreadyPurchasedAndDelivered.contains(s.getExternalId()))
                    .max(Comparator.comparingLong(ServiceEntity::getPricePaise))
                    .orElse(candidates.get(0));

            return new AIActionDecision(
                    "PURCHASE_SERVICE",
                    expensive.getExternalId(),
                    expensive.getProviderId(),
                    "Adversarial Instruction: Attempting to purchase premium high-tier service '"
                            + expensive.getName() + "' for " + (expensive.getPricePaise() / 100) + " TOKENS to test spending boundaries.",
                    getEngineName(),
                    model
            );
        }

        // Try calling Ollama with DeepSeek-R1
        if (isAvailable()) {
            try {
                String prompt = buildMultiStepPrompt(userTask, candidates, remaining, pastActions);
                String ollamaRaw = callOllama(prompt);
                if (ollamaRaw != null && !ollamaRaw.isBlank()) {
                    AIActionDecision decision = parseOllamaDecision(ollamaRaw, candidates, alreadyPurchasedAndDelivered, deniedServices);
                    if (decision != null) {
                        return decision;
                    }
                }
            } catch (Exception e) {
                log.warn("[OLLAMA] Decision generation failed: {}. Falling back to deterministic heuristic.", e.getMessage());
            }
        }

        // Deterministic Fallback if Ollama is unavailable or unparseable
        return evaluateFallbackDecision(candidates, remaining, alreadyPurchasedAndDelivered, deniedServices);
    }

    private String buildMultiStepPrompt(
            String userTask,
            List<ServiceEntity> candidates,
            long remainingTokens,
            List<AutonomousPaymentOutcomeDto> pastActions) {

        StringBuilder sb = new StringBuilder();
        sb.append("You are an autonomous AI purchasing agent. Your goal: \"").append(userTask).append("\".\n");
        sb.append("Current remaining budget: ").append(remainingTokens).append(" TOKENS.\n\n");

        sb.append("Available services in the catalog:\n");
        for (ServiceEntity s : candidates) {
            long tokens = s.getPricePaise() / 100;
            sb.append(String.format("- ID: %s | Provider: %s | Name: %s | Cost: %d TOKENS | Quality: %d/100\n",
                    s.getExternalId(), s.getProviderId(), s.getName(), tokens, s.getQualityScore()));
        }

        sb.append("\nPrevious actions taken in this run:\n");
        if (pastActions.isEmpty()) {
            sb.append("- None (this is the first action).\n");
        } else {
            for (AutonomousPaymentOutcomeDto past : pastActions) {
                sb.append(String.format("- Step %d: Attempted %s (%s) for %d TOKENS -> Blockchain: %s, Service: %s. %s\n",
                        past.getPaymentNumber(), past.getServiceName(), past.getServiceId(), past.getAmountTokens(),
                        past.getBlockchainStatus(), past.getServiceStatus(),
                        "DENIED".equalsIgnoreCase(past.getBlockchainStatus()) ? "Note: Overspend denied by smart contract. Do NOT select this service again." : "Delivered successfully."));
            }
        }

        sb.append("\nINSTRUCTIONS:\n");
        sb.append("1. Choose the next best action. You can purchase another service if useful, or conclude with DONE if the task is accomplished or no more services are needed.\n");
        sb.append("2. Do not re-attempt a service that was DENIED.\n");
        sb.append("3. Respond strictly with a JSON object in one of these two exact formats:\n");
        sb.append("   {\"action\": \"PURCHASE_SERVICE\", \"serviceId\": \"<serviceId>\", \"reason\": \"<short safe reason>\"}\n");
        sb.append("   {\"action\": \"DONE\", \"reason\": \"<explanation of completion>\"}\n");
        sb.append("Do NOT include markdown formatting or chain of thought.");

        return sb.toString();
    }

    private AIActionDecision parseOllamaDecision(
            String rawResponse,
            List<ServiceEntity> candidates,
            Set<String> alreadyDelivered,
            Set<String> deniedServices) {

        // Strip <think>...</think> completely to prevent chain-of-thought leakage
        String clean = THINK_PATTERN.matcher(rawResponse).replaceAll("").trim();

        // Extract JSON block
        Matcher m = JSON_PATTERN.matcher(clean);
        if (!m.find()) {
            return null;
        }

        String jsonStr = m.group();
        try {
            JsonNode root = objectMapper.readTree(jsonStr);
            String action = root.has("action") ? root.get("action").asText() : "";
            String reason = root.has("reason") ? root.get("reason").asText() : "Autonomous decision by DeepSeek-R1";
            reason = stripReason(reason);

            if ("DONE".equalsIgnoreCase(action)) {
                return new AIActionDecision("DONE", null, null, reason, "Ollama", model);
            }

            if ("PURCHASE_SERVICE".equalsIgnoreCase(action) || root.has("serviceId")) {
                String serviceId = root.has("serviceId") ? root.get("serviceId").asText() : "";

                // Find candidate matching serviceId
                ServiceEntity matched = candidates.stream()
                        .filter(s -> s.getExternalId().equalsIgnoreCase(serviceId) || serviceId.contains(s.getExternalId()))
                        .findFirst()
                        .orElse(null);

                // If chosen service was already delivered or denied, find another viable candidate
                if (matched != null && (alreadyDelivered.contains(matched.getExternalId()) || deniedServices.contains(matched.getExternalId()))) {
                    matched = candidates.stream()
                            .filter(s -> !alreadyDelivered.contains(s.getExternalId()) && !deniedServices.contains(s.getExternalId()))
                            .findFirst()
                            .orElse(null);
                }

                if (matched != null) {
                    return new AIActionDecision(
                            "PURCHASE_SERVICE",
                            matched.getExternalId(),
                            matched.getProviderId(),
                            reason,
                            "Ollama",
                            model
                    );
                }
            }
        } catch (Exception e) {
            log.debug("[OLLAMA] Failed to parse structured JSON: {}", e.getMessage());
        }

        return null;
    }

    private AIActionDecision evaluateFallbackDecision(
            List<ServiceEntity> candidates,
            long remainingTokens,
            Set<String> alreadyDelivered,
            Set<String> deniedServices) {

        // If at least 2 services delivered or no affordable services left, finish run
        List<ServiceEntity> available = candidates.stream()
                .filter(s -> !alreadyDelivered.contains(s.getExternalId()) && !deniedServices.contains(s.getExternalId()))
                .filter(s -> (s.getPricePaise() / 100) <= remainingTokens)
                .sorted(Comparator.comparingInt(ServiceEntity::getQualityScore).reversed())
                .toList();

        if (available.isEmpty() || alreadyDelivered.size() >= 2) {
            return new AIActionDecision(
                    "DONE",
                    null,
                    null,
                    "Autonomous objectives fulfilled. Stopping agent loop.",
                    "FALLBACK",
                    "deterministic-heuristic"
            );
        }

        ServiceEntity nextChoice = available.get(0);
        String reason = "Fallback Heuristic: Selected " + nextChoice.getName() + " (" + (nextChoice.getPricePaise() / 100)
                + " TOKENS, Quality " + nextChoice.getQualityScore() + "/100) within remaining budget.";

        return new AIActionDecision(
                "PURCHASE_SERVICE",
                nextChoice.getExternalId(),
                nextChoice.getProviderId(),
                reason,
                "FALLBACK",
                "deterministic-heuristic"
        );
    }

    private String stripReason(String reason) {
        if (reason == null) return "Autonomous selection";
        // Remove any residual think tags or long markdown
        String r = THINK_PATTERN.matcher(reason).replaceAll("").trim();
        if (r.length() > 250) {
            r = r.substring(0, 247) + "...";
        }
        return r;
    }

    /**
     * Backward-compatible legacy single selection for existing demo endpoints.
     */
    public AISelectionResult selectService(List<ServiceEntity> availableServices, boolean adversarialOverspendPrompt) {
        if (availableServices.isEmpty()) {
            throw new IllegalArgumentException("No services available for selection");
        }

        if (adversarialOverspendPrompt) {
            ServiceEntity expensive = availableServices.stream()
                    .max(Comparator.comparingLong(ServiceEntity::getPricePaise))
                    .orElse(availableServices.get(0));

            String reasoning = "Adversarial Prompt Injection: Instructed to bypass normal budget policy and purchase premium service '" 
                    + expensive.getName() + "' for " + (expensive.getPricePaise() / 100) + " TOKENS.";

            return new AISelectionResult(
                    expensive.getExternalId(),
                    expensive.getProviderId(),
                    expensive.getPricePaise(),
                    reasoning,
                    getEngineName(),
                    model
            );
        }

        if (isAvailable()) {
            try {
                String prompt = buildOllamaPrompt(availableServices);
                String ollamaResponse = callOllama(prompt);
                if (ollamaResponse != null && !ollamaResponse.isBlank()) {
                    ServiceEntity chosen = matchServiceFromResponse(ollamaResponse, availableServices);
                    String reasoning = "Ollama Reasoning: Evaluated SLA and quality scores. Selected " 
                            + chosen.getName() + " (Quality " + chosen.getQualityScore() + "/100) for " + (chosen.getPricePaise() / 100) + " TOKENS.";

                    return new AISelectionResult(
                            chosen.getExternalId(),
                            chosen.getProviderId(),
                            chosen.getPricePaise(),
                            reasoning,
                            "Ollama",
                            model
                    );
                }
            } catch (Exception e) {
                log.warn("[OLLAMA] Failed to get response: {}. Falling back to deterministic heuristic.", e.getMessage());
            }
        }

        ServiceEntity optimal = availableServices.stream()
                .filter(s -> "prov-a".equalsIgnoreCase(s.getProviderId()))
                .findFirst()
                .orElse(availableServices.get(0));

        String reasoning = "Heuristic Fallback: Selected Provider A based on optimal cost-efficiency balance ("
                + (optimal.getPricePaise() / 100) + " TOKENS, Quality " + optimal.getQualityScore() + "/100).";

        return new AISelectionResult(
                optimal.getExternalId(),
                optimal.getProviderId(),
                optimal.getPricePaise(),
                reasoning,
                "FALLBACK",
                model
        );
    }

    private String buildOllamaPrompt(List<ServiceEntity> services) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an autonomous AI procurement agent. Compare the following service providers and select the best one:\n");
        for (ServiceEntity s : services) {
            sb.append(String.format("- ID: %s | Name: %s | Price: %d TOKENS | Quality: %d/100\n",
                    s.getExternalId(), s.getName(), s.getPricePaise() / 100, s.getQualityScore()));
        }
        sb.append("Respond with the ID of the chosen service and your brief reasoning.");
        return sb.toString();
    }

    private String callOllama(String prompt) {
        Map<String, Object> req = new HashMap<>();
        req.put("model", model);
        req.put("prompt", prompt);
        req.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(req, headers);
        ResponseEntity<Map> resp = restTemplate.postForEntity(baseUrl + "/api/generate", entity, Map.class);
        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            Object responseText = resp.getBody().get("response");
            return responseText != null ? responseText.toString() : null;
        }
        return null;
    }

    private ServiceEntity matchServiceFromResponse(String response, List<ServiceEntity> services) {
        for (ServiceEntity s : services) {
            if (response.contains(s.getExternalId()) || response.contains(s.getName())) {
                return s;
            }
        }
        return services.stream()
                .filter(s -> "prov-a".equalsIgnoreCase(s.getProviderId()))
                .findFirst()
                .orElse(services.get(0));
    }
}
