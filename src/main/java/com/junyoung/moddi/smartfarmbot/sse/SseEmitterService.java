package com.junyoung.moddi.smartfarmbot.sse;

import com.junyoung.moddi.smartfarmbot.dto.SensorDataResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 연결된 SSE 클라이언트(emitter) 목록을 관리하고, 새 센서 데이터를 모두에게 전송한다.
 */
@Service
public class SseEmitterService {

    // 여러 클라이언트가 동시에 연결/해제할 수 있어 CopyOnWriteArrayList로 스레드 안전하게 관리
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        // 타임아웃 없이(연결 끊길 때까지 유지) emitter 생성
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        emitters.add(emitter);
        return emitter;
    }

    public void broadcast(SensorDataResponse data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("sensor-data").data(data));
            } catch (IOException e) {
                // 전송 실패한 emitter(연결 끊김 등)는 목록에서 제거
                emitters.remove(emitter);
            }
        }
    }
}
