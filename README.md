## Overseas-RealDataCollector
한국 투자 증권 API를 이용하여 해외주식(뉴욕, 나스닥, 아멕스) 실시간 체결가를 조회하는 프로그램입니다.

### 개발 환경
- **Java**: 17
- **Spring Boot**: 3.2.2
- **라이브러리**:
  - `spring-boot-starter-websocket`
  - `json-simple 1.1.1`
  - `lombok`

### 환경 설정

RealDataCollector.json 설정파일
```json
{
  "Authentication": {
    "GrantType": "client_credentials",
    "AppKey": "[KIS에서 발급된 AppKey 값을 여기에 입력하세요]",
    "SecretKey": "[KIS에서 발급된 SecretKey 값을 여기에 입력하세요]"
  },
  "Stocks": {
    "AMEX": [
      {
        "Symbol": "[주식 거래 코드를 입력하세요]",
        "Name": "[주식명을 입력하세요]"
      }
    ],
    "NASDAQ": [
      {
        "Symbol": "[주식 거래 코드를 입력하세요]",
        "Name": "[주식명을 입력하세요]"
      }
    ],
    "NYSE": [
      {
        "Symbol": "[주식 거래 코드를 입력하세요]",
        "Name": "[주식명을 입력하세요]"
      }
    ]
  },
  "Settings": {
    "WebsocketAccessKeyUrl": "https://openapi.koreainvestment.com:9443/oauth2/Approval",
    "OverseasStockQuoteUrl": "ws://ops.koreainvestment.com:21000/tryitout/HDFSCNT0",
    "EnableDebugLog": "false",
    "AutoClosingTime": "[자동 종료 시간을 입력하세요]"
  }
```
- `Authentication`: 인증 관련 설정을 관리합니다.
    - `GrantType` [필수]: 권한 부여 타입으로 "client_credentials"를 입력해 주세요.
    - `AppKey` [필수]: 한국투자증권 홈페이지에서 발급받은 appkey를 입력해 주세요. 보안을 위해 절대 노출되지 않도록 주의해 주세요
    - `SecretKey` [필수]:  한국투자증권 홈페이지에서 발급받은 secretkey를 입력해 주세요. 보안을 위해 절대 노출되지 않도록 주의해 주세요.
- `Stocks`: 체결가를 조회할 주식 정보를 관리합니다.
    - `Symbol` [필수]: 주식 거래 코드(티커 심볼)를 입력해 주세요. ex) TSLA, SPY
    - `Name`:  `Symbol`에 대한 추가 정보를 제공하는 항목으로, 필수값은 아닙니다. ex) Tesla, SPDR S&P 500 ETF
- `Settings`: 프로그램의 기타 설정을 관리합니다.
    - `WebsocketAccessKeyUrl` [필수]: 실시간 웹소켓 접속키를 발급받기 위한 도메인 URL로`https://openapi.koreainvestment.com:9443/oauth2/Approval`을 입력해 주세요. 추후 경로가 변경될 경우, KIS API 문서를 참고하여 업데이트 해주세요.
    - `OverseasStockQuoteUrl` [필수]: 해외주식 실시간 지연 체결가를 조회하기 위한 도메인 URL로 `ws://ops.koreainvestment.com:21000/tryitout/HDFSCNT0`을 입력해 주세요. 추후 경로가 변경될 경우, KIS API 문서를 참고하여 업데이트 해주세요.
    - `EnableDebugLog` [필수]: 웹소켓 서버에서 수신받는 모든 데이터를 가공하지 않고 그대로 기록할지 여부를 설정합니다. `true` 또는 `false` 를 입력해 주세요.
    - `AutoClosingTime` [필수]: 프로그램 종료를 원하는 시간을 미국 시간 기준으로 HHss 형식으로 입력해 주세요.
        - ex1) 오전 2시 3분의 경우 -> 0203
        - ex2) 오후 6시의 경우 -> 1800

- AMEX에만 주식을 등록하고 NASDAQ, NYSE에는 주식을 등록하지 않는 경우 아래와 같이 키는 남겨두어야 합니다.
```json
"Stocks": {
 "AMEX": [
    {
      "Symbol": "YINN",
      "Name": "Direcxion Daily FTSE China Bull 3X Shares"
    }
  ],
  "NASDAQ": [],
  "NYSE": []
}
```

### 빌드하고 실행하기
#### MAC
1. `./gradlew build --exclude-task test`
2. `cd build/libs`
3. `java -jar overseas-0.0.1-SNAPSHOT.jar`
4. 실행 확인

#### 윈도우
1. 콘솔로 이동 명령 프롬프트(cmd)로 이동
2. `gradlew build --exclude-task test`
3. `cd build/libs`
4. `java -jar overseas-0.0.1-SNAPSHOT.jar`
5. 실행 확인

