package com.sortech.b4demo;

import com.sun.net.httpserver.HttpServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * 느린 "외부 API" 역할을 하는 작은 서버 (포트 9090).
 * 우리 서버(8080)와 스레드를 따로 쓰기 때문에, 진짜 외부 서비스를 호출하는 것처럼 동작한다.
 *
 * 사용: GET http://localhost:9090/slow?ms=1000  → 1초 뒤에 응답
 */
@Component
public class FakeExternalApi {

    public static final String BASE_URL = "http://localhost:9090";

    private HttpServer server;

    @PostConstruct
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(9090), 0);
        server.createContext("/slow", exchange -> {
            long ms = parseMs(exchange.getRequestURI().getQuery());
            try {
                Thread.sleep(ms); // 외부 API가 처리하는 데 걸리는 시간
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] body = ("done after " + ms + "ms").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    @PreDestroy
    public void stop() {
        server.stop(0);
    }

    private long parseMs(String query) {
        if (query != null && query.startsWith("ms=")) {
            return Long.parseLong(query.substring(3));
        }
        return 1000;
    }
}
