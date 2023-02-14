package stock.overseas.websocket;

import lombok.extern.slf4j.Slf4j;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@Slf4j
@ClientEndpoint
public class WebSocketClient {

    private Session userSession;
    private MessageHandler messageHandler;

    public WebSocketClient() {
        userSession = null;
    }

    public Session connect(URI endpointURI) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            userSession = container.connectToServer(this, endpointURI);
            return userSession;
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
