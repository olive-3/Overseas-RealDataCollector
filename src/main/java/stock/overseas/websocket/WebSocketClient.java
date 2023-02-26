package stock.overseas.websocket;

import lombok.extern.slf4j.Slf4j;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

@Slf4j
@ClientEndpoint
public class WebSocketClient {

    private static final WebSocketClient instance = new WebSocketClient();
    private Session userSession = null;
    private MessageHandler messageHandler;

    public static WebSocketClient getInstance() {
        return instance;
    }

    private WebSocketClient() {}

    public Session getUserSession() {
        return userSession;
    }

    public void connect(URI endpointURI) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

    public void sendMessage() {
        try {
            this.userSession.getAsyncRemote().sendPing(ByteBuffer.wrap(new String("ping").getBytes("UTF-8")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnOpen
    public void onOpen(Session userSession) {
        log.info("opening websocket");
        this.userSession = userSession;
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        log.info("closing websocket");
        log.info("closing reason = {}", reason);
        this.userSession = null;
    }

    @OnMessage
    public void onMessage(String message) throws org.json.simple.parser.ParseException, IOException {
        if(this.messageHandler != null) {
            this.messageHandler.handleMessage(message);
        }
    }

    @OnError
    public void connectionError(Throwable t) {
        t.printStackTrace();
    }

}
