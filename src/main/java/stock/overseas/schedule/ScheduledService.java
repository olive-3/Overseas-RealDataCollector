package stock.overseas.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import stock.overseas.websocket.WebSocketClient;

import javax.websocket.Session;
import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class ScheduledService {

    private WebSocketClient client = WebSocketClient.getInstance();

    @Scheduled(cron = "0 0/1 * * * *")
    public void maintainConnetion() {
        Session session = client.getUserSession();
        if(session != null) {
            client.sendMessage();
        }
    }

    @Scheduled(cron = "0 1 10 * * *")
    public void close() {
        Session session = client.getUserSession();
        try {
            session.close();
            log.info("{} : websocket closed", LocalDateTime.now());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
