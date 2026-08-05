package com.junyoung.moddi.smartfarmbot.llm;

import com.junyoung.moddi.smartfarmbot.domain.DeviceType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Gemini function calling에 넘길 함수(도구) 스펙 정의.
 * - analyze_pattern: 스마트 분석("현재 패턴 분석해 줘")
 * - control_device: 장치 제어("~켜줘")
 */
public final class GeminiToolDefinitions {

    private GeminiToolDefinitions() {
    }

    public static ArrayNode tools(ObjectMapper objectMapper) {
        ArrayNode functionDeclarations = objectMapper.createArrayNode();
        functionDeclarations.add(analyzePatternDeclaration(objectMapper));
        functionDeclarations.add(controlDeviceDeclaration(objectMapper));

        ObjectNode toolEntry = objectMapper.createObjectNode();
        toolEntry.set("functionDeclarations", functionDeclarations);

        ArrayNode tools = objectMapper.createArrayNode();
        tools.add(toolEntry);
        return tools;
    }

    private static ObjectNode analyzePatternDeclaration(ObjectMapper objectMapper) {
        ObjectNode declaration = objectMapper.createObjectNode();
        declaration.put("name", "analyze_pattern");
        declaration.put("description", "최근 50개 센서 데이터의 평균(온도/습도/토양수분/CO2/조도)을 조회해 현재 온실 패턴을 분석한다.");

        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put("type", "OBJECT");
        parameters.set("properties", objectMapper.createObjectNode());
        declaration.set("parameters", parameters);
        return declaration;
    }

    private static ObjectNode controlDeviceDeclaration(ObjectMapper objectMapper) {
        ObjectNode deviceProperty = objectMapper.createObjectNode();
        deviceProperty.put("type", "STRING");
        deviceProperty.put("description",
            "SPRINKLER(스프링클러/토양수분), FAN(환풍기/온도), DEHUMIDIFIER(제습기/습도), "
                + "GROW_LIGHT(보광등/조도), CO2_GENERATOR(탄산가스/CO2) 중 하나");
        ArrayNode enumValues = objectMapper.createArrayNode();
        for (DeviceType type : DeviceType.values()) {
            enumValues.add(type.name());
        }
        deviceProperty.set("enum", enumValues);

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("device", deviceProperty);

        ArrayNode required = objectMapper.createArrayNode();
        required.add("device");

        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put("type", "OBJECT");
        parameters.set("properties", properties);
        parameters.set("required", required);

        ObjectNode declaration = objectMapper.createObjectNode();
        declaration.put("name", "control_device");
        declaration.put("description", "지정한 장치를 가동해 해당 센서 값을 정상 범위로 복구한다.");
        declaration.set("parameters", parameters);
        return declaration;
    }
}
