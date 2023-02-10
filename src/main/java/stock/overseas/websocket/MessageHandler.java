package stock.overseas.websocket;

import org.json.simple.parser.ParseException;

import java.io.IOException;

public interface MessageHandler {

    void handleMessage(String message) throws ParseException, IOException;
}
