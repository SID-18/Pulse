package com.pulse.ai.client;

import com.pulse.ai.dto.IncidentRagRecommendationResponse;
import com.pulse.ai.dto.RagIncidentDocument;

import java.util.List;

public interface IncidentRagClient {

    int index(List<RagIncidentDocument> incidents);

    IncidentRagRecommendationResponse analyze(RagIncidentDocument incident);
}
