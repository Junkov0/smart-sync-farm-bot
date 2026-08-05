package com.junyoung.moddi.smartfarmbot.dto;

import jakarta.validation.constraints.NotBlank;

// 챗봇에게 보내는 사용자 메시지
public record ChatRequest(
    @NotBlank(message = "메시지를 입력해주세요.") String message
) {
}
