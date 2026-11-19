package com.sortech.b4demo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * 데모 ① 응답이 느림
 * - /demo1/sequential : 외부 API 3개를 순서대로 호출 (블로킹이 3번 이어짐) → 약 3초
 * - /demo1/parallel   : 3개를 동시에 맡기고 한 번만 기다림 → 약 1초
 */
@RestController
public class Demo1SlowController {

    private static final String URL_A = FakeExternalApi.BASE_URL + "/slow?ms=1000";
    private static final String URL_B = FakeExternalApi.BASE_URL + "/slow?ms=1000";
    private static final String URL_C = FakeExternalApi.BASE_URL + "/slow?ms=1000";

    private final RestTemplate restTemplate;
    private final ExecutorService pool;

    public Demo1SlowController(@Qualifier("noTimeoutRestTemplate") RestTemplate restTemplate,
                               ExecutorService apiPool) {
        this.restTemplate = restTemplate;
        this.pool = apiPool;
    }

    @GetMapping("/demo1/sequential")
    public Map<String, Object> sequential() {
        long start = System.currentTimeMillis();

        // 호출마다 응답이 올 때까지 블로킹 (각 1초)
        String a = restTemplate.getForObject(URL_A, String.class);
        String b = restTemplate.getForObject(URL_B, String.class);
        String c = restTemplate.getForObject(URL_C, String.class);

        return result("순차 호출", start);
    }

    @GetMapping("/demo1/parallel")
    public Map<String, Object> parallel() {
        long start = System.currentTimeMillis();

        // 세 호출을 동시에 맡기기
        var a = supplyAsync(() -> call(URL_A), pool);
        var b = supplyAsync(() -> call(URL_B), pool);
        var c = supplyAsync(() -> call(URL_C), pool);

        // 가장 느린 1개만큼만 기다림
        CompletableFuture.allOf(a, b, c).join();

        return result("동시 호출", start);
    }

    private String call(String url) {
        return restTemplate.getForObject(url, String.class);
    }

    private Map<String, Object> result(String type, long start) {
        return Map.of("방식", type, "걸린 시간(ms)", System.currentTimeMillis() - start);
    }
}
