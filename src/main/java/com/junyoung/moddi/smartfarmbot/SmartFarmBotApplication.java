package com.junyoung.moddi.smartfarmbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 더미 데이터 생성 스케줄러 활성화
public class SmartFarmBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartFarmBotApplication.class, args);
	}

}
