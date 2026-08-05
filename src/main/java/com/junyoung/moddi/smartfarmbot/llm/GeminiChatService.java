package com.junyoung.moddi.smartfarmbot.llm;

import com.junyoung.moddi.smartfarmbot.domain.DeviceType;
import com.junyoung.moddi.smartfarmbot.service.DeviceControlService;
import com.junyoung.moddi.smartfarmbot.service.SensorAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * 사용자 메시지를 Gemini에 보내고, 필요하면 function calling으로 로컬 함수(분석/제어)를 실행한 뒤
 * 최종 자연어 응답을 만들어 돌려준다. 함수 호출은 1회 왕복만 지원한다(데모 범위에 충분).
 */
@Service
@RequiredArgsConstructor
public class GeminiChatService {

    private static final String SYSTEM_INSTRUCTION = """
        당신은 스마트팜 온실을 모니터링하는 AI 봇입니다.
        사용자 질문에 한국어로 친절하고 간결하게 답하세요.
        현재 센서 패턴을 물어보면 analyze_pattern 함수를, 장치를 켜달라는 요청이면 control_device 함수를 호출하세요.
        """;

    private final GeminiClient geminiClient;
    private final SensorAnalysisService sensorAnalysisService;
    private final DeviceControlService deviceControlService;
    private final ObjectMapper objectMapper;

    public String chat(String userMessage) {
        ArrayNode contents = objectMapper.createArrayNode();
        contents.add(userTurn(userMessage));

        JsonNode firstPart = firstPart(geminiClient.generateContent(buildRequest(contents)));

        if (!firstPart.has("functionCall")) {
            return firstPart.path("text").asString("죄송합니다, 응답을 생성하지 못했습니다.");
        }

        JsonNode functionCall = firstPart.get("functionCall");
        String functionName = functionCall.path("name").asString();
        String functionResult = executeFunction(functionName, functionCall.path("args"));

        // thoughtSignature 등 모델이 돌려준 part의 다른 필드까지 그대로 보존해서 재전송해야 함
        contents.add(modelFunctionCallTurn(firstPart));
        contents.add(functionResponseTurn(functionName, functionResult));

        JsonNode secondPart = firstPart(geminiClient.generateContent(buildRequest(contents)));
        return secondPart.path("text").asString(functionResult);
    }

    private String executeFunction(String name, JsonNode args) {
        return switch (name) {
            case "analyze_pattern" -> sensorAnalysisService.analyzeRecentPattern();
            case "control_device" -> deviceControlService.controlDevice(DeviceType.valueOf(args.path("device").asString()));
            default -> "알 수 없는 함수 호출입니다: " + name;
        };
    }

    private JsonNode buildRequest(ArrayNode contents) {
        ObjectNode request = objectMapper.createObjectNode();
        request.set("contents", contents);
        request.set("tools", GeminiToolDefinitions.tools(objectMapper));

        ArrayNode systemParts = objectMapper.createArrayNode();
        systemParts.add(objectMapper.createObjectNode().put("text", SYSTEM_INSTRUCTION));
        ObjectNode systemInstruction = objectMapper.createObjectNode();
        systemInstruction.set("parts", systemParts);
        request.set("systemInstruction", systemInstruction);

        return request;
    }

    private ObjectNode userTurn(String message) {
        ArrayNode parts = objectMapper.createArrayNode();
        parts.add(objectMapper.createObjectNode().put("text", message));

        ObjectNode turn = objectMapper.createObjectNode();
        turn.put("role", "user");
        turn.set("parts", parts);
        return turn;
    }

    private ObjectNode modelFunctionCallTurn(JsonNode modelPart) {
        ArrayNode parts = objectMapper.createArrayNode();
        parts.add(modelPart);

        ObjectNode turn = objectMapper.createObjectNode();
        turn.put("role", "model");
        turn.set("parts", parts);
        return turn;
    }

    private ObjectNode functionResponseTurn(String name, String result) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("result", result);

        ObjectNode functionResponse = objectMapper.createObjectNode();
        functionResponse.put("name", name);
        functionResponse.set("response", response);

        ObjectNode part = objectMapper.createObjectNode();
        part.set("functionResponse", functionResponse);
        ArrayNode parts = objectMapper.createArrayNode();
        parts.add(part);

        ObjectNode turn = objectMapper.createObjectNode();
        turn.put("role", "user");
        turn.set("parts", parts);
        return turn;
    }

    private JsonNode firstPart(JsonNode response) {
        return response.path("candidates").path(0).path("content").path("parts").path(0);
    }
}
