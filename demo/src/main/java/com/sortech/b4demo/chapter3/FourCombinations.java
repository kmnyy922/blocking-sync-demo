package com.sortech.b4demo.chapter3;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 발표 3장 "코드로 보기": 동기/비동기 × 블로킹/논블로킹 4가지 조합.
 * Spring 없이 main 으로 바로 실행할 수 있다. (IntelliJ에서 main 옆 ▶ 버튼)
 *
 * callApi() 는 1초 걸리는 외부 API 호출이라고 생각하면 된다.
 * 로그의 시간과 스레드 이름을 보면 "누가 멈췄는지, 누가 결과를 챙겼는지" 알 수 있다.
 */
public class FourCombinations {

    private static final ExecutorService pool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws Exception {
        syncBlocking();
        syncNonBlocking();
        asyncNonBlocking();
        asyncBlocking();
        pool.shutdown();
    }

    /** ① Sync + Blocking: 결과가 올 때까지 멈추고, 결과도 직접 받음 */
    static void syncBlocking() {
        title("① Sync + Blocking");
        log("API 호출 시작");
        String user = callApi();              // 1초 동안 이 줄에서 멈춤
        log("결과 받음: " + user);             // 다음 줄에서 바로 사용
    }

    /** ② Sync + Non-Blocking: 바로 돌아오지만, 완료는 직접 확인(폴링) */
    static void syncNonBlocking() throws Exception {
        title("② Sync + Non-Blocking (폴링)");
        Future<String> future = pool.submit(FourCombinations::callApi);
        log("맡기고 바로 돌아옴");
        int checks = 0;
        while (!future.isDone()) {            // "이제 왔어?"
            checks++;
            doOtherWork();
        }
        log("확인 " + checks + "번 만에 결과 받음: " + future.get());
    }

    /** ③ Async + Non-Blocking: 멈추지 않고, 끝나면 알려줌(콜백) */
    static void asyncNonBlocking() throws Exception {
        title("③ Async + Non-Blocking (콜백)");
        CompletableFuture<Void> done = CompletableFuture
                .supplyAsync(FourCombinations::callApi, pool)
                .thenAccept(user -> log("콜백으로 결과 받음: " + user));
        log("기다리지 않고 바로 다음 일 진행");
        done.join();                          // 데모 출력 순서를 맞추려고 끝날 때까지 대기
    }

    /** ④ Async + Blocking: 비동기로 맡겼지만 바로 기다려서 결국 멈춤 (흔한 실수) */
    static void asyncBlocking() throws Exception {
        title("④ Async + Blocking (흔한 실수)");
        CompletableFuture<String> future = CompletableFuture.supplyAsync(FourCombinations::callApi, pool);
        log("비동기로 맡김");
        String user = future.get();           // 바로 기다림 → 이 스레드가 1초 멈춤
        log("결국 기다려서 결과 받음: " + user);
    }

    // ----- 도우미 -----

    private static String callApi() {
        sleep(1000);
        return "user-1";
    }

    private static void doOtherWork() {
        sleep(200);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void title(String t) {
        System.out.println();
        System.out.println("==== " + t + " ====");
    }

    private static void log(String msg) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        System.out.printf("[%s] [%-24s] %s%n", time, Thread.currentThread().getName(), msg);
    }
}
