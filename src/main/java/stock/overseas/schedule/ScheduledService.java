package stock.overseas.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import stock.overseas.gui.MyGUI;
import stock.overseas.websocket.WebSocketClient;

import javax.websocket.Session;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class ScheduledService {

    private WebSocketClient client = WebSocketClient.getInstance();
    private MyGUI myGUI = MyGUI.getInstance();

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
            myGUI.actionPerformed(LocalDateTime.now(), "소켓 닫힘 => 실패");
        }
    }
}
