package com.sortech.b4demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 외부 API와 전혀 상관없는 가벼운 요청. 데모 ②에서 서버가 멈췄는지 확인하는 용도. */
@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
