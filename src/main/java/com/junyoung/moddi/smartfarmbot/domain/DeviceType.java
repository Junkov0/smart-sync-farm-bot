package com.junyoung.moddi.smartfarmbot.domain;

// 봇 제어 명령("~켜줘")으로 정상화할 수 있는 장치 5종
public enum DeviceType {
    SPRINKLER,      // 스프링클러 - 토양수분 정상화
    FAN,            // 환풍기 - 온도 정상화
    DEHUMIDIFIER,   // 제습기 - 습도 정상화
    GROW_LIGHT,     // 보광등 - 조도 정상화
    CO2_GENERATOR   // 탄산가스 발생기 - CO2 정상화
}
