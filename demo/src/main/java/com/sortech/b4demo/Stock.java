package com.sortech.b4demo;

/** 재고. 실제 서비스라면 DB 테이블의 한 행이라고 생각하면 된다. */
public class Stock {

    private int quantity;

    public Stock(int quantity) {
        this.quantity = quantity;
    }

    /** 락 없음: 여러 스레드가 동시에 들어오면 서로의 변경을 덮어쓴다. */
    public void decrease() {
        int current = quantity;     // A, B가 동시에 100을 읽음
        simulateDbDelay();          // DB 조회·계산에 걸리는 짧은 시간
        quantity = current - 1;     // 둘 다 99로 저장
    }

    /** 락 있음: 한 번에 한 스레드만 들어오고, 나머지는 끝날 때까지 블로킹된다. */
    public synchronized void decreaseWithLock() {
        int current = quantity;
        simulateDbDelay();
        quantity = current - 1;
    }

    public int getQuantity() {
        return quantity;
    }

    private void simulateDbDelay() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
