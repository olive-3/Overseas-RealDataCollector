### 개요
한국 투자 증권 API를 이용하여 해외주식(뉴욕, 나스닥, 아멕스) 실시간 체결가를 조회하는 프로그램입니다.

### 개발 환경
- **Java**: 17
- **Spring Boot**: 3.2.2
- **라이브러리**:
  - `spring-boot-starter-websocket`
  - `json-simple 1.1.1`
  - `lombok`

### 설정 파일
RealDataCollector.json파일
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
        "Symbol": "",
        "Name": ""
      }
    ],
    "NYSE": [
      {
        "Symbol": "",
        "Name": ""
      }
    ]
  },
  "Settings": {
    "WebsocketAccessKeyUrl": "https://openapi.koreainvestment.com:9443/oauth2/Approval",
    "OverseasStockQuoteUrl": "ws://ops.koreainvestment.com:21000/tryitout/HDFSCNT0",
    "EnableDebugLog": "",
    "AutoClosingTime": ""
  }
```
- ```Symbol```: 주식 거래 코드(종목 코드)를 입력해 주세요.
- ```Name```: ```Symbol```을 알아보기 위한 추가 정보로 필수값이 아닙니다.
- ```EnableDebugLog```: 웹소켓 서버에서 수신하는 모든 메시지를 기록할지 여부를 설정하는 키입니다. true 또는 false 값을 입력해 주세요.
- ```AutoClosingTime```: 프로그램 종료를 원하는 시간을 미국 시간 기준으로 HHss 형식으로 입력해 주세요.
  - 오전 2시 3분의 경우 -> 0203
  - 오후 6시의 경우 -> 1800

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
- RealDataCollector.json 예시
```json
{
  "Authentication": {
    "GrantType": "client_credentials",
    "AppKey": "발급받은 AppKey",
    "SecretKey": "발급받은 SecretKey"
  },
  "Stocks": {
    "AMEX": [
      {
        "Symbol": "SPY",
        "Name": "SPDR S&P 500 ETF"
      },
      {
        "Symbol": "SOXL",
        "Name": "Direcxion Daily Semiconductor Bull 3X Shares"
      },
      {
        "Symbol": "SOXS",
        "Name": "Direcxion Daily Semiconductor Bear 3X Shares"
      }
    ],
    "NASDAQ": [
      {
        "Symbol": "TSLA",
        "Name": "Tesla"
      }
    ],
    "NYSE": [
      {
        "Symbol": "XOM",
        "Name": "Exxon Mobil"
      }
    ]
  },
  "Settings": {
    "WebsocketAccessKeyUrl": "https://openapi.koreainvestment.com:9443/oauth2/Approval",
    "OverseasStockQuoteUrl": "ws://ops.koreainvestment.com:21000/tryitout/HDFSCNT0",
    "EnableDebugLog": "false",
    "AutoClosingTime": "1800"
  }
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

