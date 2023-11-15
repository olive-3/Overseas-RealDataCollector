package stock.overseas.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import stock.overseas.domain.Stock;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Slf4j
@ClientEndpoint
public class WebSocketClient {

    private String approvalKey;
    private List<Stock> stockInfoList;
    private Session userSession = null;
    private MessageHandler messageHandler;
    private final String endpoint = "ws://ops.koreainvestment.com:21000";

    public WebSocketClient(String approvalKey, List<Stock> stockInfoList) {
        this.approvalKey = approvalKey;
        this.stockInfoList = stockInfoList;
    }

    public void connectAndSubscribe()  {
        //connect
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            URI endpointURI = new URI(endpoint);
            container.connectToServer(this, endpointURI);
        } catch (URISyntaxException | DeploymentException | IOException e) {
            log.info("[{}] {}", LocalDateTime.now(), "WebSocket 연결 => 실패");
        }

        //subscribe
        subscribe(approvalKey, stockInfoList);
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opend.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        log.info("[{}] {}", LocalDateTime.now(), "WebSocket 연결 => 성공");
        this.userSession = userSession;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed;
     * @param reason
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        log.info("[{}] {}", LocalDateTime.now(), "WebSocket 닫힘 => 성공");
        this.userSession = null;

        log.info("[{}] {}", LocalDateTime.now(), "WebSocket 재접속 시도");
        connectAndSubscribe();
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        try {
            if(this.messageHandler != null) {
                this.messageHandler.handleMessage(message);
            }
        } catch (IOException e) {
            log.info("[{}] {}", LocalDateTime.now(), "파일 작성 중 오류 발생");
        }
    }

    public void addMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

    private void subscribe(String approvalKey, List<Stock> stockInfoList) {

        for (Stock stock : stockInfoList) {
            sendMessage(createSendMessage(approvalKey, stock.getTrKey()));
            // 위치 수정 할 예정
            String message = "[" + stock.getSymbol() + "] " + stock.getStockName() + " => 성공";
            log.info("[{}] {}", LocalDateTime.now(), message);
        }

        String completionMessage = "총 " + stockInfoList.size() + " 종목 실시간 체결 데이터 등록 완료";
        log.info("[{}] {}", LocalDateTime.now(), completionMessage);
    }

    //"{"header": {"tr_type":"1", "approval_key":" + approvalKey + ", "custtype":""}, "body": {"input": {"tr_id":"", "tr_key":" + trKey + "}}}
    private String createSendMessage(String approvalKey, String trKey) {

        //json header
        HashMap<String, Object> jsonHeader = new HashMap<>();
        jsonHeader.put("tr_type", "1");
        jsonHeader.put("approval_key", approvalKey);
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
            sendJson = new ObjectMapper().writeValueAsString(json);
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
        }

        return sendJson;
    }
}
