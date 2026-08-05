package com.junyoung.moddi.smartfarmbot.llm;

import com.junyoung.moddi.smartfarmbot.exception.LlmCommunicationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

/**
 * Gemini generateContent REST API를 직접 호출하는 얇은 클라이언트.
 * 별도 SDK 없이 RestClient로 요청/응답 JSON을 그대로 주고받는다.
 */
@Component
public class GeminiClient {

    private final RestClient restClient;
    private final String model;
    private final String apiKey;

    public GeminiClient(
        @Value("${gemini.base-url}") String baseUrl,
        @Value("${gemini.model}") String model,
        @Value("${gemini.api-key}") String apiKey
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.model = model;
        this.apiKey = apiKey;
    }

    public JsonNode generateContent(JsonNode requestBody) {
        try {
            return restClient.post()
                .uri("/models/{model}:generateContent?key={apiKey}", model, apiKey)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new LlmCommunicationException("Gemini API 호출에 실패했습니다.", e);
        }
    }
}
