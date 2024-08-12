package stock.overseas.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
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
import java.util.stream.Collectors;

@Slf4j
@ClientEndpoint
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WebSocketClient {

    private String approvalKey;
    private List<Stock> stockInfoList;
    private List<String> trKeyList;
    private URI endpointURI;
    private Session userSession;
    private MessageHandler messageHandler;
    private Timer timer;
    private boolean enableDebugLog;

    private volatile boolean running = true;
    private static final long KEEP_ALIVE_INTERVAL_MS = 10000; // 100초

    public WebSocketClient(String approvalKey, List<Stock> stockInfoList, String overseasStockQuoteUrl, boolean enableDebugLog) {
        this.approvalKey = approvalKey;
        this.stockInfoList = stockInfoList;
        this.trKeyList = stockInfoList.stream()
                .map(stock -> stock.getTrKey())
                .collect(Collectors.toList());
        try {
            this.endpointURI = new URI(overseasStockQuoteUrl);
        } catch (URISyntaxException e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "해외주식 실시간 지연 체결가 도메인을 확인해 주세요.");
            throw new RuntimeException();
        }
        this.enableDebugLog = enableDebugLog;
        this.timer = new Timer();
    }

    public void connect() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            container.connectToServer(this, endpointURI);
        } catch (DeploymentException | IOException e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "WebSocket 연결 중 문제가 발생했습니다.");
            throw new RuntimeException(e);
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
        log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "WebSocket 연결 => 성공");

        addMessageHandler(new MessageHandler(trKeyList, enableDebugLog));

        //서버로 메세지 전송
        for (Stock stock : stockInfoList) {
            sendMessage(createSendMessage(stock.getTrKey()));
            Thread.sleep(500);
        }

//        startPing();
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed;
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
//        stopPing();
        log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), reason.getReasonPhrase());
        log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "WebSocket 닫힘 => 성공");

        //재접속 시도
        log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "WebSocket 재접속 시도");
        connect();
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) throws IOException, ParseException {
        try {
            if (this.messageHandler != null) {
                this.messageHandler.handleMessage(message);
            }
        } catch (CustomWebsocketException e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), e.getMessage());
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "프로그램 종료");
            System.exit(-1);
        } catch (RuntimeException e) {
            if (userSession != null) {
                CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, e.getMessage());
                userSession.close(closeReason);
            }
        }
    }

    /**
     * Send a message.
     *
     * @param message
     */
    public void sendMessage(String message) {
        try {
            this.userSession.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "WebSocket 메세지 전송 중 문제가 발생했습니다.");
            throw new RuntimeException(e);
        }
    }

    /**
     * add a messagehandler
     *
     * @param messageHandler
     */
    public void addMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
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
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            sendJson = objectMapper.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "해외 주식 실시간지연체결가 HTTP 요청 메세지 생성 중 오류가 발생했습니다.");
            throw new RuntimeException();
        }

        return sendJson;
    }

//    private void startPing() {
//        running = true;
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                if(running) {
//                    try {
//                        System.out.println("WebSocketClient.run");
//                        userSession.getBasicRemote().sendPing(ByteBuffer.allocate(0));
//                    } catch (IOException e) {
//                        log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "웹소켓 연결 유지를 위한 Ping 메세지 전송 중 오류가 발생했습니다.");
//                        return;
//                    }
//                }
//            }
//        }, 0, KEEP_ALIVE_INTERVAL_MS); //100초
//    }
//
//    private void stopPing() {
//        if (timer != null) {
//            timer.cancel();
//            running = false;
//        }
//    }
}
