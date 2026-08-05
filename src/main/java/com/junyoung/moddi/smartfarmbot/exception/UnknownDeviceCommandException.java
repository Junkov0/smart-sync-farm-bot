package com.junyoung.moddi.smartfarmbot.exception;

// LLM이 function calling으로 넘긴 device 값이 DeviceType 어디에도 해당하지 않을 때 던진다
public class UnknownDeviceCommandException extends RuntimeException {

    public UnknownDeviceCommandException(String device) {
        super("알 수 없는 장치입니다: " + device);
    }
}
