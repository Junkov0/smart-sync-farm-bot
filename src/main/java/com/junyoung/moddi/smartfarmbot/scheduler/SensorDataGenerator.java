package com.junyoung.moddi.smartfarmbot.scheduler;

import com.junyoung.moddi.smartfarmbot.domain.SensorData;
import com.junyoung.moddi.smartfarmbot.dto.SensorDataResponse;
import com.junyoung.moddi.smartfarmbot.repository.SensorDataRepository;
import com.junyoung.moddi.smartfarmbot.sse.SseEmitterService;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 2초마다 가상 온실 센서 데이터를 생성해 DB에 저장하고, SSE로 실시간 push한다.
 * 매번 전체 범위에서 독립적으로 랜덤값을 뽑으면 정상/경고 상태가 너무 자주 뒤바뀌어
 * 알림이 정신없이 울린다. 대신 직전 값에서 조금씩만 움직이는 random walk로 생성해
 * 실제 센서처럼 완만하게 변하고, 경고 상태도 한동안 유지되게 한다.
 * 직전 값은 DB의 최신 row에서 읽으므로 챗봇 제어(장치 정상화)로 값이 바뀌어도
 * 다음 tick이 그 값을 그대로 이어받는다.
 */
@Component
@RequiredArgsConstructor
public class SensorDataGenerator {

    private final SensorDataRepository sensorDataRepository;
    private final SseEmitterService sseEmitterService;

    @Scheduled(fixedRate = 2000)
    public void generate() {
        SensorData latest = sensorDataRepository.findFirstByOrderByCreatedAtDesc()
            .orElseGet(() -> new SensorData(24, 60, 55, 420, 500));

        // 온도: 18~40도 (경고 기준 35도 이상)
        double temperature = walk(latest.getTemperature(), 0.8, 18, 40);
        // 습도: 40~95% (경고 기준 85% 이상)
        double humidity = walk(latest.getHumidity(), 2.0, 40, 95);
        // 토양수분: 10~80% (경고 기준 30% 이하)
        double soilMoisture = walk(latest.getSoilMoisture(), 2.0, 10, 80);
        // CO2: 300~500ppm (경고 기준 350ppm 미만)
        double co2 = walk(latest.getCo2(), 6.0, 300, 500);
        // 조도: 50~800lux (경고 기준 200lux 미만)
        double light = walk(latest.getLight(), 20.0, 50, 800);

        SensorData sensorData = new SensorData(temperature, humidity, soilMoisture, co2, light);
        SensorData saved = sensorDataRepository.save(sensorData);

        sseEmitterService.broadcast(SensorDataResponse.from(saved));
    }

    private double walk(double current, double maxStep, double min, double max) {
        double next = current + ThreadLocalRandom.current().nextDouble(-maxStep, maxStep);
        return Math.min(max, Math.max(min, next));
    }
}
