package com.sortech.b4demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 데모 ③ 데이터가 꼬임
 * 재고 100개에 주문 100건이 동시에 들어오는 상황을 스레드 20개로 재현한다.
 * - /demo3/no-lock   : 남은 재고가 0이 아님 (경쟁 상태)
 * - /demo3/with-lock : 남은 재고가 정확히 0
 */
@RestController
public class Demo3StockController {

    private static final int INITIAL = 100;
    private static final int ORDERS = 100;

    @GetMapping("/demo3/no-lock")
    public Map<String, Object> noLock() throws InterruptedException {
        Stock stock = new Stock(INITIAL);
        runConcurrently(stock::decrease);
        return result("락 없음", stock);
    }

    @GetMapping("/demo3/with-lock")
    public Map<String, Object> withLock() throws InterruptedException {
        Stock stock = new Stock(INITIAL);
        runConcurrently(stock::decreaseWithLock);
        return result("락 있음 (synchronized)", stock);
    }

    private void runConcurrently(Runnable order) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(20);
        CountDownLatch done = new CountDownLatch(ORDERS);
        for (int i = 0; i < ORDERS; i++) {
            pool.submit(() -> {
                try {
                    order.run();
                } finally {
                    done.countDown();
                }
            });
        }
        done.await();     // 주문 100건이 모두 끝날 때까지 기다림
        pool.shutdown();
    }

    private Map<String, Object> result(String type, Stock stock) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("방식", type);
        map.put("처음 재고", INITIAL);
        map.put("주문 수", ORDERS);
        map.put("남은 재고 (정답: 0)", stock.getQuantity());
        return map;
    }
}
