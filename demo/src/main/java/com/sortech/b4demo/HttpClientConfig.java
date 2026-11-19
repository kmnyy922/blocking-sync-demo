package com.sortech.b4demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class HttpClientConfig {

    /** 타임아웃이 없는 RestTemplate: 응답이 올 때까지 무한정 기다린다. */
    @Bean
    public RestTemplate noTimeoutRestTemplate() {
        return new RestTemplate();
    }

    /** 타임아웃이 있는 RestTemplate: 연결 1초, 응답 2초가 넘으면 포기한다. */
    @Bean
    public RestTemplate timeoutRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1000);
        factory.setReadTimeout(2000);
        return new RestTemplate(factory);
    }

    /** 외부 API를 동시에 호출할 때 쓰는 별도 스레드 풀 (요청 스레드와 분리). */
    @Bean(destroyMethod = "shutdown")
    public ExecutorService apiPool() {
        return Executors.newFixedThreadPool(10);
    }
}
