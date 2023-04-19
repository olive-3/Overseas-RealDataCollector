package stock.overseas.websocket;

import lombok.extern.slf4j.Slf4j;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;

@Slf4j
@ClientEndpoint
public class WebSocketClient {

    private Session userSession = null;
    private MessageHandler messageHandler;
    private static final WebSocketClient instance = new WebSocketClient();

    public static WebSocketClient getInstance() {
        return instance;
    }

    private WebSocketClient() {}

    public Session getUserSession() {
        return userSession;
    }

    public void connect(URI endpointURI) throws DeploymentException, IOException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, endpointURI);
    }

    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

    public void sendMessage() {
        try {
            this.userSession.getAsyncRemote().sendPing(ByteBuffer.wrap(("ping").getBytes("UTF-8")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
    }

    @OnClose
    public void onClose() {
        this.userSession = null;
        log.info("[{}] {}", LocalDateTime.now(), "소켓 닫힘 => 성공");
    }

    @OnMessage
    public void onMessage(String message) {
        if(this.messageHandler != null) {
            try {
                this.messageHandler.handleMessage(message);
            } catch (IOException e) {
                log.info("[{}] {}", LocalDateTime.now(), "파일 작성 중 오류 발생");
            }
        }
    }

    @OnError
    public void connectionError(Throwable t) {
        t.printStackTrace();
    }
}
