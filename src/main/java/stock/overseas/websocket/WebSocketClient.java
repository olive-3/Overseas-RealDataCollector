package stock.overseas.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import stock.overseas.domain.Stock;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@ClientEndpoint
public class WebSocketClient {

    private String approvalKey;
    private List<Stock> stockInfoList;
    List<String> trKeyList = new ArrayList<>();

    private Session userSession = null;
    private MessageHandler messageHandler;

    public WebSocketClient(String approvalKey, List<Stock> stockInfoList, List<String> trKeyList) {
        this.approvalKey = approvalKey;
        this.stockInfoList = stockInfoList;
        this.trKeyList = trKeyList;
    }

    public void connect(URI endpointURI) throws DeploymentException, IOException {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (DeploymentException | IOException e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "WebSocket 연결 중 문제가 발생하였습니다.");
            throw e;
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

        // add listener
        this.messageHandler = new MessageHandler(trKeyList);

        //send message to websocket
        subscribeStocks();
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed;
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) throws URISyntaxException, DeploymentException, IOException {
        this.userSession = null;
        log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), reason.getReasonPhrase());
        log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "WebSocket 닫힘 => 성공");

        //재접속
        log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "WebSocket 재접속");
        connect(new URI("ws://ops.koreainvestment.com:21000"));
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
        } catch (IllegalArgumentException e) {
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

    @OnMessage
    public void onMessage(ByteBuffer bytes) {
        System.out.println("Handle byte buffer");
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

    public void subscribeStocks() throws InterruptedException {
        for (Stock stock : stockInfoList) {
            sendMessage(createSendMessage(approvalKey, stock.getTrKey()));
            Thread.sleep(500);
        }

        // wait 10 seconds for messages from websocket
//        Thread.sleep(1000);
    }

    //"{"header": {"tr_type":"1", "approval_key":" + approvalKey + ", "custtype":""}, "body": {"input": {"tr_id":"", "tr_key":" + trKey + "}}}
    private String createSendMessage(String approvalKey, String trKey) {

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
            sendJson = new ObjectMapper().writeValueAsString(json);
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
        }

        return sendJson;
    }
}
