package com.junyoung.moddi.smartfarmbot.exception;

// Gemini API 호출(네트워크 오류, 4xx/5xx 응답 등)이 실패했을 때 던진다
public class LlmCommunicationException extends RuntimeException {

    public LlmCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
