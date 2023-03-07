package stock.overseas.websocket;

import java.io.IOException;

public interface MessageHandler {

    void handleMessage(String message) throws IOException;
}
