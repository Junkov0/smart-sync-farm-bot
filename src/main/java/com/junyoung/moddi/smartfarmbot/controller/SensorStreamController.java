package com.junyoung.moddi.smartfarmbot.controller;

import com.junyoung.moddi.smartfarmbot.sse.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sensor")
@RequiredArgsConstructor
public class SensorStreamController {

    private final SseEmitterService sseEmitterService;

    // 프론트엔드가 이 엔드포인트를 구독하면 이후 생성되는 센서 데이터를 실시간으로 push 받는다
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return sseEmitterService.subscribe();
    }
}
