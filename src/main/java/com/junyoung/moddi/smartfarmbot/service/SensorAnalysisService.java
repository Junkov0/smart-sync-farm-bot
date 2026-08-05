package com.junyoung.moddi.smartfarmbot.service;

import com.junyoung.moddi.smartfarmbot.domain.SensorData;
import com.junyoung.moddi.smartfarmbot.repository.SensorDataRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * "현재 패턴 분석해 줘" 같은 스마트 분석 요청을 처리한다.
 * 최근 50건 센서 데이터의 평균을 계산해 사람이 읽기 쉬운 요약 문자열로 반환한다.
 */
@Service
@RequiredArgsConstructor
public class SensorAnalysisService {

    private final SensorDataRepository sensorDataRepository;

    @Transactional(readOnly = true)
    public String analyzeRecentPattern() {
        List<SensorData> recent = sensorDataRepository.findTop50ByOrderByCreatedAtDesc();

        if (recent.isEmpty()) {
            return "아직 분석할 센서 데이터가 없습니다.";
        }

        double avgTemperature = recent.stream().mapToDouble(SensorData::getTemperature).average().orElse(0);
        double avgHumidity = recent.stream().mapToDouble(SensorData::getHumidity).average().orElse(0);
        double avgSoilMoisture = recent.stream().mapToDouble(SensorData::getSoilMoisture).average().orElse(0);
        double avgCo2 = recent.stream().mapToDouble(SensorData::getCo2).average().orElse(0);
        double avgLight = recent.stream().mapToDouble(SensorData::getLight).average().orElse(0);

        return "최근 %d건 평균 - 온도: %.1f도, 습도: %.1f%%, 토양수분: %.1f%%, CO2: %.0fppm, 조도: %.0flux"
            .formatted(recent.size(), avgTemperature, avgHumidity, avgSoilMoisture, avgCo2, avgLight);
    }
}
