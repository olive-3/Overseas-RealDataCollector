package stock.overseas.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import stock.overseas.domain.Stock;
import stock.overseas.exception.CustomWebsocketException;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

@Slf4j
@ClientEndpoint
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WebSocketClient {

    private volatile boolean running = true;
    private static final long KEEP_ALIVE_INTERVAL_MS = 100000;   //100초

    private String approvalKey;
    private List<Stock> stockInfoList;
    private List<String> trKeyList;
    private URI endpointURI;
    private Session userSession;
    private MessageHandler messageHandler;
    private boolean enableDebugLog;
    private Timer timer;
    private ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketClient(String approvalKey, List<Stock> stockInfoList, String overseasStockQuoteUrl, boolean enableDebugLog) {
        this.approvalKey = approvalKey;
        this.stockInfoList = stockInfoList;
        this.trKeyList = extractTrKeyList(stockInfoList);
        this.endpointURI = createURI(overseasStockQuoteUrl);
        this.enableDebugLog = enableDebugLog;
    }

    private List<String> extractTrKeyList(List<Stock> stockInfoList) {
        return stockInfoList.stream()
                .map(stock -> stock.getTrKey())
                .collect(Collectors.toList());
    }

    private URI createURI(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            log.error("해외주식 실시간 지연 체결가 URL을 확인해 주세요.");
            throw new RuntimeException();
        }
    }

    public void connect() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            container.connectToServer(this, endpointURI);
        } catch (DeploymentException e) {
            log.error("해외주식 실시간 지연 체결가 URL을 확인해 주세요.");
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.info("WebSocket 연결 중 문제가 발생했습니다.");
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) throws InterruptedException {
        this.userSession = userSession;
        log.info("WebSocket 연결 => 성공");

        addMessageHandler(new MessageHandler(trKeyList, enableDebugLog));

        //서버로 메세지 전송
        for (Stock stock : stockInfoList) {
            sendMessage(createSendMessage(stock.getTrKey()));
            Thread.sleep(500);
        }

        startHeartbeat();
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed;
     * @param reason      the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
        log.info("[" + reason.getReasonPhrase() + "] WebSocket 닫힘");
        stopHeartBeat();

        //재접속 시도
        log.info("WebSocket 재접속 시도");
        connect();
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        try {
            if (this.messageHandler != null) {
                this.messageHandler.handleMessage(message);
            }
        } catch (CustomWebsocketException e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), e.getMessage());
            log.info("프로그램이 종료되었습니다.");
            System.exit(-1);
        } catch (RuntimeException e) {
            if (userSession != null) {
                CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, e.getMessage());
                try {
                    userSession.close(closeReason);
                } catch (IOException ex) {
                    log.warn("웹소켓 연결 종료 중 오류가 발생했습니다.");
                }
            }
        }
    }

    /**
     * Send a message.
     *
     * @param message
     */
    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

    /**
     * add a messagehandler
     *
     * @param messageHandler
     */
    public void addMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    /**
     * Websocket 커넥션 유지
     */
    public void startHeartbeat() {
        running = true;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (running) {
                    if (userSession != null && userSession.isOpen()) {
                        sendMessage(createPingPongMessage());
                    }
                }
            }
        }, 0, KEEP_ALIVE_INTERVAL_MS); //100초
    }

    //"{"header": {"tr_type":"1", "approval_key":" + approvalKey + ", "custtype":""}, "body": {"input": {"tr_id":"", "tr_key":" + trKey + "}}}
    private String createSendMessage(String trKey) {
        //json header
        HashMap<String, Object> jsonHeader = new HashMap<>();
        jsonHeader.put("tr_type", "1");
        jsonHeader.put("approval_key", approvalKey);
        jsonHeader.put("content-type", "utf-8");
        jsonHeader.put("custtype", "P");

        //json body
        HashMap<String, Object> jsonInputBody = new HashMap<>();
        jsonInputBody.put("tr_id", "HDFSCNT0");
        jsonInputBody.put("tr_key", trKey);

        HashMap<String, Object> json = new HashMap<>();
        json.put("header", jsonHeader);

        HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put("input", jsonInputBody);

        json.put("body", jsonBody);

        String sendJson = null;
        try {
            sendJson = objectMapper.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            log.error("해외 주식 실시간 지연 체결가 Subscribe 메세지 생성 중 오류가 발생했습니다.");
            throw new RuntimeException(e);
        }

        return sendJson;
    }

    //{"header":{"tr_id":"PINGPONG","datetime":""}}
    private String createPingPongMessage() {
        //json header
        HashMap<String, Object> jsonHeader = new HashMap<>();
        jsonHeader.put("tr_id", "PINGPONG");
        String now = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        jsonHeader.put("datetime", now);

        HashMap<String, Object> json = new HashMap<>();
        json.put("header", jsonHeader);

        String sendJson = null;
        try {
            sendJson = objectMapper.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            log.error("WebSocket PINGPONG 메세지 생성 중 오류가 발생했습니다.");
            throw new RuntimeException(e);
        }

        return sendJson;
    }

    private void stopHeartBeat() {
        if (timer != null) {
            timer.cancel();
            running = false;
        }
    }
}
