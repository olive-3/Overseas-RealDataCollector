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

    @Scheduled(cron = "${sendPingTime.cron}")
    public void maintainConnection() {
        Session session = client.getUserSession();
        if(session != null) {
            client.sendMessage();
        }
    }

    @Scheduled(cron = "${closingTime.cron}")
    public void close() {
        Session session = client.getUserSession();
        try {
            session.close();
        } catch (IOException e) {
            log.info("[{}] {}", LocalDateTime.now(), "소켓 닫힘 => 실패");
        }
    }
}
