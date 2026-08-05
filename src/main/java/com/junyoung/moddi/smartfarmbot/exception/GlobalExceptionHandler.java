package com.junyoung.moddi.smartfarmbot.exception;

import com.junyoung.moddi.smartfarmbot.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getDefaultMessage())
            .orElse("잘못된 요청입니다.");
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    @ExceptionHandler(LlmCommunicationException.class)
    public ResponseEntity<ErrorResponse> handleLlmCommunication(LlmCommunicationException ex) {
        log.error("Gemini API 호출 실패", ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(UnknownDeviceCommandException.class)
    public ResponseEntity<ErrorResponse> handleUnknownDevice(UnknownDeviceCommandException ex) {
        log.warn(ex.getMessage());
        return ResponseEntity.internalServerError().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("처리되지 않은 예외", ex);
        return ResponseEntity.internalServerError().body(new ErrorResponse("서버 오류가 발생했습니다."));
    }
}
