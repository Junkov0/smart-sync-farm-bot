package com.junyoung.moddi.smartfarmbot.service;

import com.junyoung.moddi.smartfarmbot.domain.DeviceType;
import com.junyoung.moddi.smartfarmbot.domain.SensorData;
import com.junyoung.moddi.smartfarmbot.dto.SensorDataResponse;
import com.junyoung.moddi.smartfarmbot.repository.SensorDataRepository;
import com.junyoung.moddi.smartfarmbot.sse.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * "스프링클러 켜줘" 같은 제어 명령을 받아 해당 장치가 담당하는 센서 값만 정상 수준으로
 * 강제 조정한 새 데이터를 생성한다. 나머지 값은 최근 데이터를 그대로 유지한다.
 */
@Service
@RequiredArgsConstructor
public class DeviceControlService {

    // 장치별 정상화 목표값 (경고 임계값에서 벗어난 정상 범위 중간값)
    private static final double NORMAL_TEMPERATURE = 24;
    private static final double NORMAL_HUMIDITY = 60;
    private static final double NORMAL_SOIL_MOISTURE = 55;
    private static final double NORMAL_CO2 = 420;
    private static final double NORMAL_LIGHT = 500;

    private final SensorDataRepository sensorDataRepository;
    private final SseEmitterService sseEmitterService;

    @Transactional
    public String controlDevice(DeviceType deviceType) {
        SensorData latest = sensorDataRepository.findFirstByOrderByCreatedAtDesc()
            .orElseGet(() -> new SensorData(NORMAL_TEMPERATURE, NORMAL_HUMIDITY, NORMAL_SOIL_MOISTURE, NORMAL_CO2, NORMAL_LIGHT));

        SensorData normalized = switch (deviceType) {
            case SPRINKLER -> new SensorData(latest.getTemperature(), latest.getHumidity(), NORMAL_SOIL_MOISTURE, latest.getCo2(), latest.getLight());
            case FAN -> new SensorData(NORMAL_TEMPERATURE, latest.getHumidity(), latest.getSoilMoisture(), latest.getCo2(), latest.getLight());
            case DEHUMIDIFIER -> new SensorData(latest.getTemperature(), NORMAL_HUMIDITY, latest.getSoilMoisture(), latest.getCo2(), latest.getLight());
            case GROW_LIGHT -> new SensorData(latest.getTemperature(), latest.getHumidity(), latest.getSoilMoisture(), latest.getCo2(), NORMAL_LIGHT);
            case CO2_GENERATOR -> new SensorData(latest.getTemperature(), latest.getHumidity(), latest.getSoilMoisture(), NORMAL_CO2, latest.getLight());
        };

        SensorData saved = sensorDataRepository.save(normalized);
        sseEmitterService.broadcast(SensorDataResponse.from(saved));

        return describe(deviceType);
    }

    private String describe(DeviceType deviceType) {
        return switch (deviceType) {
            case SPRINKLER -> "스프링클러를 가동해 토양수분을 정상 범위로 회복시켰습니다.";
            case FAN -> "환풍기를 가동해 온도를 정상 범위로 낮췄습니다.";
            case DEHUMIDIFIER -> "제습기를 가동해 습도를 정상 범위로 낮췄습니다.";
            case GROW_LIGHT -> "보광등을 켜서 조도를 정상 범위로 올렸습니다.";
            case CO2_GENERATOR -> "탄산가스 발생기를 가동해 CO2 농도를 정상 범위로 올렸습니다.";
        };
    }
}
