package com.junyoung.moddi.smartfarmbot.common;

/**
 * 센서별 경고(WARNING) 판정 임계값. DTO 변환, 추후 제어 명령 로직에서 공통으로 사용.
 */
public final class SensorThresholds {

    private SensorThresholds() {
    }

    // 온도: 35도 이상이면 과열
    public static final double TEMPERATURE_WARNING = 35;

    // 습도: 85% 이상이면 과습
    public static final double HUMIDITY_WARNING = 85;

    // 토양수분: 30% 이하면 부족
    public static final double SOIL_MOISTURE_WARNING = 30;

    // CO2: 350ppm 미만이면 부족
    public static final double CO2_WARNING = 350;

    // 조도: 200lux 미만이면 일조량 부족
    public static final double LIGHT_WARNING = 200;
}
