package com.pulse.ai.client;

import com.pulse.ai.dto.IncidentRagRecommendationResponse;
import com.pulse.ai.dto.RagIncidentDocument;
import com.pulse.ai.dto.RagIndexRequest;
import com.pulse.ai.dto.RagIndexResult;
import com.pulse.ai.exception.RagServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class HttpIncidentRagClient implements IncidentRagClient {

    private final RestClient restClient;

    public HttpIncidentRagClient(
        @Value("${pulse.ai.rag.base-url}") String baseUrl
    ) {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(new SimpleClientHttpRequestFactory())
            .build();
    }

    @Override
    public int index(List<RagIncidentDocument> incidents) {
        try {
            RagIndexResult result = restClient.post()
                .uri("/api/v1/incidents/index")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RagIndexRequest(incidents))
                .retrieve()
                .body(RagIndexResult.class);
            if (result == null) {
                throw new IllegalStateException("RAG service returned no indexing result.");
            }
            return result.indexedCount();
        } catch (Exception exception) {
            throw new RagServiceUnavailableException(exception);
        }
    }

    @Override
    public IncidentRagRecommendationResponse analyze(RagIncidentDocument incident) {
        try {
            IncidentRagRecommendationResponse result = restClient.post()
                .uri("/api/v1/incidents/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .body(incident)
                .retrieve()
                .body(IncidentRagRecommendationResponse.class);
            if (result == null) {
                throw new IllegalStateException("RAG service returned no recommendation.");
            }
            return result;
        } catch (Exception exception) {
            throw new RagServiceUnavailableException(exception);
        }
    }
}
