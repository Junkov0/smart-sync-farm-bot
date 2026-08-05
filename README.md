# Smart-Sync Farm Bot

(주)모디의 '스마트팜 통합 솔루션' 아키텍처를 모방한 스마트팜 AI 모니터링 봇 & 대시보드.

## 개요

- 온실 센서 데이터(온도/습도/토양수분/CO2/조도) 실시간 생성 및 모니터링
- 정상 범위 이탈 시 SSE 기반 실시간 경고 알림
- LLM Function Calling으로 상태 조회 및 제어(복구) 명령 수행
- 프론트엔드 대시보드에서 이벤트별 시각 효과 및 챗봇 UI 제공

## 기술 스택

- Backend: Java 17, Spring Boot, Spring Data JPA, H2
- Frontend: HTML, CSS, Vanilla JS, Chart.js
- 통신: Server-Sent Events(SSE)
- AI: LLM Function Calling

## 경고 임계값

| 항목 | 기준 |
| --- | --- |
| 온도 | 35도 이상 |
| 습도 | 85% 이상 |
| 토양수분 | 30% 이하 |
| 조도 | 200 lux 미만 |
| CO2 | 350 ppm 미만 |

## 브랜치 전략

`main` (배포) ← `develop` (통합) ← `feat/기능명` (기능 개발)
