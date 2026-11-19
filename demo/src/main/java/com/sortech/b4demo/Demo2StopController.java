package com.sortech.b4demo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 데모 ② 서버가 멈춤 (application.yml 에서 요청 스레드를 10개로 제한)
 * - /demo2/no-timeout   : 10초 걸리는 외부 API를 타임아웃 없이 호출 → 요청 스레드가 10초 동안 묶임
 * - /demo2/with-timeout : 2초 타임아웃 → 빨리 실패하고 스레드를 돌려줌
 *
 * 둘 중 하나를 10번 동시에 호출한 뒤 /health 가 바로 응답하는지 확인한다.
 */
@RestController
public class Demo2StopController {

    private static final String SLOW_URL = FakeExternalApi.BASE_URL + "/slow?ms=10000";

    private final RestTemplate noTimeout;
    private final RestTemplate withTimeout;

    public Demo2StopController(@Qualifier("noTimeoutRestTemplate") RestTemplate noTimeout,
                               @Qualifier("timeoutRestTemplate") RestTemplate withTimeout) {
        this.noTimeout = noTimeout;
        this.withTimeout = withTimeout;
    }

    @GetMapping("/demo2/no-timeout")
    public Map<String, Object> noTimeout() {
        long start = System.currentTimeMillis();
        // 타임아웃 없음 → 10초 동안 이 요청 스레드가 묶임
        noTimeout.getForObject(SLOW_URL, String.class);
        return Map.of("결과", "성공", "걸린 시간(ms)", System.currentTimeMillis() - start);
    }

    @GetMapping("/demo2/with-timeout")
    public Map<String, Object> withTimeout() {
        long start = System.currentTimeMillis();
        try {
            withTimeout.getForObject(SLOW_URL, String.class);
            return Map.of("결과", "성공", "걸린 시간(ms)", System.currentTimeMillis() - start);
        } catch (ResourceAccessException e) {
            // 2초 넘으면 포기 → 빨리 실패하고 스레드를 돌려줌
            return Map.of("결과", "타임아웃으로 빠르게 실패", "걸린 시간(ms)", System.currentTimeMillis() - start);
        }
    }
}
