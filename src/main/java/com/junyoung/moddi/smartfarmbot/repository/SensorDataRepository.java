package com.junyoung.moddi.smartfarmbot.repository;

import com.junyoung.moddi.smartfarmbot.domain.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
}
