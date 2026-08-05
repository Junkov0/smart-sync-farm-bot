package com.junyoung.moddi.smartfarmbot.dto;

import com.junyoung.moddi.smartfarmbot.common.SensorThresholds;
import com.junyoung.moddi.smartfarmbot.domain.SensorData;
import java.time.LocalDateTime;

/**
 * SSE로 프론트엔드에 내려주는 센서 데이터 응답.
 * 항목별 경고 여부(*Warning)를 미리 계산해 프론트에서 바로 이벤트 판단만 하면 되게 한다.
 */
public record SensorDataResponse(
    Long id,
    double temperature,
    double humidity,
    double soilMoisture,
    double co2,
    double light,
    boolean temperatureWarning,
    boolean humidityWarning,
    boolean soilMoistureWarning,
    boolean co2Warning,
    boolean lightWarning,
    LocalDateTime createdAt
) {

    public static SensorDataResponse from(SensorData sensorData) {
        return new SensorDataResponse(
            sensorData.getId(),
            sensorData.getTemperature(),
            sensorData.getHumidity(),
            sensorData.getSoilMoisture(),
            sensorData.getCo2(),
            sensorData.getLight(),
            sensorData.getTemperature() >= SensorThresholds.TEMPERATURE_WARNING,
            sensorData.getHumidity() >= SensorThresholds.HUMIDITY_WARNING,
            sensorData.getSoilMoisture() <= SensorThresholds.SOIL_MOISTURE_WARNING,
            sensorData.getCo2() < SensorThresholds.CO2_WARNING,
            sensorData.getLight() < SensorThresholds.LIGHT_WARNING,
            sensorData.getCreatedAt()
        );
    }
}
