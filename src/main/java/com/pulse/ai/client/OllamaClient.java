package com.pulse.ai.client;

import com.pulse.ai.exception.AiServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OllamaClient {

    private final RestClient restClient;
    private final String model;

    public OllamaClient(
        @Value("${pulse.ai.ollama.base-url}") String baseUrl,
        @Value("${pulse.ai.ollama.model}") String model
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.model = model;
    }

    public String generate(String prompt) {
        try {
            OllamaGenerateResponse response = restClient.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new OllamaGenerateRequest(model, prompt, false))
                .retrieve()
                .body(OllamaGenerateResponse.class);
            if (response == null || response.response() == null) {
                throw new IllegalStateException("Ollama returned no generated response.");
            }
            return response.response().trim();
        } catch (RestClientException | IllegalStateException exception) {
            throw new AiServiceUnavailableException(exception);
        }
    }

    public String model() {
        return model;
    }

    private record OllamaGenerateRequest(
        String model,
        String prompt,
        boolean stream
    ) {
    }

    private record OllamaGenerateResponse(String response) {
    }
}
