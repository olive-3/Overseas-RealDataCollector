package stock.overseas.websocket;

import lombok.extern.slf4j.Slf4j;
import stock.overseas.directory.Stock;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class WebSocketService  {

    private MessageHandler messageHandler;
    private WebSocketClient client;

    public WebSocketService(List<String> trKeyList) {
        this.messageHandler = new MessageHandlerImpl(trKeyList);
        this.client = WebSocketClient.getInstance();
    }

    public void getConnection() throws DeploymentException, URISyntaxException, IOException {

        try {
            client.connect(new URI("ws://ops.koreainvestment.com:21000"));
            log.info("[{}] {}", LocalDateTime.now(), "Websocket 연결 => 성공");
        } catch (URISyntaxException | DeploymentException | IOException e) {
            throw e;
        }
    }

    public void sendMessage(String approvalKey, List<Stock> stockInfoList) {

        client.addMessageHandler(messageHandler);

        for (Stock stock : stockInfoList) {
            client.sendMessage(createSendMessage(approvalKey, stock.getTrKey()));
            String message = "[" + stock.getSymbol() + "] " + stock.getStockName() + " => 성공";
            log.info("[{}] {}", LocalDateTime.now(), message);
        }

        String completionMessage = "총 " + stockInfoList.size() + " 종목 실시간 체결 데이터 등록 완료";
        log.info("[{}] {}", LocalDateTime.now(), completionMessage);
    }

    private String createSendMessage(String approvalKey, String trKey) {

        String message = "{\n" +
                "    \"header\":\n" +
                "    {\n" +
                "        \"tr_type\":\"1\",\n" +
                "        \"approval_key\": \"" + approvalKey + "\",\n" +
                "        \"custtype\":\"P\",\n" +
                "\n" +
                "        \"content-type\": \"utf-8\"\n" +
                "    },\n" +
                "    \"body\":\n" +
                "    {\n" +
                "        \"input\":\n" +
                "        {\n" +
                "            \"tr_id\":\"HDFSCNT0\",\n" +
                "            \"tr_key\":\"" + trKey + "\"\n" +
                "        }\n" +
                "    }\n" +
                "}";

        return message;
    }
}