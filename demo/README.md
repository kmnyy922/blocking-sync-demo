# B4 데모: 블로킹 때문에 생기는 실무 문제 3가지

발표 4장 "실무 문제, 코드로 해결하기"에서 쓰는 Spring Boot 데모예요.
각 문제마다 **문제 코드 → 해결 코드**를 실행해서 결과 숫자를 비교해요.

## 실행 방법

1. IntelliJ에서 `demo` 폴더를 열기 (Gradle 프로젝트로 자동 인식, JDK 17 이상)
2. `B4DemoApplication` 실행
   - 우리 서버: `http://localhost:8080`
   - 느린 외부 API 역할 서버: `http://localhost:9090/slow?ms=1000` (앱이 같이 띄움)

## 3장 코드로 보기: 4가지 조합

`chapter3/FourCombinations.java`는 Spring 없이 `main`으로 바로 실행돼요 (main 옆 ▶ 버튼).
로그의 **시간**과 **스레드 이름**으로 누가 멈췄고 누가 결과를 챙겼는지 보여줘요.

```
==== ① Sync + Blocking ====
[..:40.973] [main          ] API 호출 시작
[..:41.982] [main          ] 결과 받음: user-1               ← main이 1초 멈춤
==== ③ Async + Non-Blocking (콜백) ====
[..:43.002] [main          ] 기다리지 않고 바로 다음 일 진행
[..:44.002] [pool-1-thread-2] 콜백으로 결과 받음: user-1      ← 다른 스레드가 알려줌
```

## 3장 ③ ④ 프론트엔드 데모

`frontend/index.html`을 브라우저(Chrome)로 더블클릭해서 열면 돼요. 서버 실행이 필요 없어요.
화면 위쪽의 **계속 올라가는 숫자**가 멈추면 메인 스레드가 블로킹된 거예요.

| 버튼 | 내용 | 확인할 것 |
|---|---|---|
| 순서대로 await / Promise.all | 가짜 API(각 1초) 3개 호출 | 약 3000ms vs 약 1000ms, 숫자는 계속 올라감 |
| 막지 않은 / 막는 결제 버튼 | 결제 버튼 연타 | 중복 결제 vs 처리 중 클릭 무시 |
| setTimeout / 반복문 3초 대기 | 3초 뒤 알림 닫기 | 반복문은 3초 동안 숫자·화면 멈춤 |
| 메인 스레드 / Web Worker 정렬 | 상품 300만 개 가격순 정렬 | 메인 스레드는 숫자 멈춤, Worker는 계속 올라감 |

## ① 응답이 느림

브라우저에서 열기:

| 주소 | 내용 | 예상 결과 |
|---|---|---|
| http://localhost:8080/demo1/sequential | 외부 API 3개 순서대로 호출 | 약 3000ms |
| http://localhost:8080/demo1/parallel | 3개 동시에 호출 | 약 1000ms |

## ② 서버가 멈춤

요청 스레드를 10개로 제한해 둔 상태(`application.yml`)에서, 10초 걸리는 API 호출을 10번 동시에 보낸 뒤 `/health`를 호출해요.

PowerShell (Windows):

```powershell
# 1) 타임아웃 없음: 느린 요청 10개를 동시에 보냄
1..10 | ForEach-Object { Start-Job { Invoke-RestMethod http://localhost:8080/demo2/no-timeout } }

# 2) 바로 /health 호출 → 약 10초 동안 응답 없음 (스레드 10개가 모두 묶여 있음)
Measure-Command { Invoke-RestMethod http://localhost:8080/health }

# 3) 타임아웃 있음으로 다시 해보기 (10초 기다렸다가 실행)
1..10 | ForEach-Object { Start-Job { Invoke-RestMethod http://localhost:8080/demo2/with-timeout } }
Measure-Command { Invoke-RestMethod http://localhost:8080/health }   # 약 2초 안에 응답

# 정리
Get-Job | Remove-Job -Force
```

Git Bash / macOS:

```bash
for i in $(seq 10); do curl -s localhost:8080/demo2/no-timeout & done
time curl -s localhost:8080/health
```

## ③ 데이터가 꼬임

| 주소 | 내용 | 예상 결과 |
|---|---|---|
| http://localhost:8080/demo3/no-lock | 재고 100개, 동시 주문 100건, 락 없음 | 남은 재고가 0이 아님 (예: 94) |
| http://localhost:8080/demo3/with-lock | 같은 조건 + `synchronized` | 남은 재고 정확히 0 |

## 파일 구성

| 파일 | 역할 |
|---|---|
| `FakeExternalApi.java` | 9090 포트의 느린 외부 API |
| `HttpClientConfig.java` | 타임아웃 없음/있음 RestTemplate, 동시 호출용 스레드 풀 |
| `Demo1SlowController.java` | ① 순차 호출 vs 동시 호출 |
| `Demo2StopController.java` | ② 타임아웃 없음 vs 있음 |
| `Stock.java`, `Demo3StockController.java` | ③ 락 없음 vs 락 있음 |
| `HealthController.java` | 서버가 살아 있는지 확인하는 가벼운 요청 |
| `chapter3/FourCombinations.java` | 3장 ① ② 동기/비동기 × 블로킹/논블로킹 4가지 조합 |
| `frontend/index.html` | 3장 ③ ④ 프론트엔드 (await 순서, 중복 클릭 막기, 타이머, Web Worker) |
