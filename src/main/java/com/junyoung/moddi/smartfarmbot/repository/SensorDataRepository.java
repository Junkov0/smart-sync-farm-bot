package com.junyoung.moddi.smartfarmbot.repository;

import com.junyoung.moddi.smartfarmbot.domain.SensorData;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    // 스마트 분석(최근 패턴 분석)용 - 최근 50건 최신순 조회
    List<SensorData> findTop50ByOrderByCreatedAtDesc();

    // 장치 제어 시 기준으로 삼을 가장 최근 데이터 1건
    Optional<SensorData> findFirstByOrderByCreatedAtDesc();
}
