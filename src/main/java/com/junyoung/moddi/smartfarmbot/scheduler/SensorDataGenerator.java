package com.junyoung.moddi.smartfarmbot.scheduler;

import com.junyoung.moddi.smartfarmbot.domain.SensorData;
import com.junyoung.moddi.smartfarmbot.repository.SensorDataRepository;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 2초마다 가상 온실 센서 데이터를 생성해 DB에 저장한다.
 * 값 범위는 정상/경고 구간을 모두 포함하도록 넓게 잡아 데모 중 경고 이벤트가 자연스럽게 발생하게 한다.
 */
@Component
@RequiredArgsConstructor
public class SensorDataGenerator {

    private final SensorDataRepository sensorDataRepository;

    @Scheduled(fixedRate = 2000)
    public void generate() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // 온도: 18~40도 (경고 기준 35도 이상)
        double temperature = random.nextDouble(18, 40);
        // 습도: 40~95% (경고 기준 85% 이상)
        double humidity = random.nextDouble(40, 95);
        // 토양수분: 10~80% (경고 기준 30% 이하)
        double soilMoisture = random.nextDouble(10, 80);
        // CO2: 300~500ppm (경고 기준 350ppm 미만)
        double co2 = random.nextDouble(300, 500);
        // 조도: 50~800lux (경고 기준 200lux 미만)
        double light = random.nextDouble(50, 800);

        SensorData sensorData = new SensorData(temperature, humidity, soilMoisture, co2, light);
        sensorDataRepository.save(sensorData);
    }
}
