package com.junyoung.moddi.smartfarmbot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 온실 센서 데이터 1건(온도/습도/토양수분/CO2/조도)을 담는 엔티티.
 * 2초마다 스케줄러가 새 row를 insert한다.
 */
@Entity
@Getter
@NoArgsConstructor
@Table(
    name = "sensor_data",
    // 최근 N건 조회(정렬)가 잦으므로 created_at에 인덱스 적용
    indexes = @Index(name = "idx_sensor_data_created_at", columnList = "created_at")
)
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 섭씨 온도 (과열 경고 기준: 35도 이상)
    private double temperature;

    // 상대 습도 % (과습 경고 기준: 85% 이상)
    private double humidity;

    // 토양 수분 % (부족 경고 기준: 30% 이하)
    private double soilMoisture;

    // 이산화탄소 ppm (부족 경고 기준: 350ppm 미만)
    private double co2;

    // 조도 lux (일조량 부족 경고 기준: 200lux 미만)
    private double light;

    // 데이터 생성 시각
    private LocalDateTime createdAt;

    public SensorData(double temperature, double humidity, double soilMoisture, double co2, double light) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.soilMoisture = soilMoisture;
        this.co2 = co2;
        this.light = light;
        this.createdAt = LocalDateTime.now();
    }
}
