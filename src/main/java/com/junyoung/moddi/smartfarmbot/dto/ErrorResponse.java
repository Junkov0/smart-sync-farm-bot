package com.junyoung.moddi.smartfarmbot.dto;

// 예외 발생 시 클라이언트에 내려주는 공통 에러 응답
public record ErrorResponse(String message) {
}
