package stock.overseas.websocket;

import stock.overseas.gui.MyGUI;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;

public class WebSocketService  {

    private MessageHandler messageHandler;
    private WebSocketClient client;
    private MyGUI myGUI;

    public WebSocketService(List<String> trKeyList) {
        this.messageHandler = new MessageHandlerImpl(trKeyList);
        this.client = WebSocketClient.getInstance();
        this.myGUI = MyGUI.getInstance();
    }

    public void getConnection() throws DeploymentException, URISyntaxException, IOException {

        try {
            client.connect(new URI("ws://ops.koreainvestment.com:21000"));
            myGUI.actionPerformed(LocalDateTime.now(), "Websocket 연결 => 성공");
        } catch (URISyntaxException | DeploymentException | IOException e) {
            throw e;
        }
    }

    public void sendMessage(String approvalKey, List<String> trKeyList) {

        client.addMessageHandler(messageHandler);

        for (String trKey : trKeyList) {
            client.sendMessage(createSendMessage(approvalKey, trKey));
            myGUI.actionPerformed(LocalDateTime.now(), trKey.substring(4) + " => 성공");
        }

        myGUI.actionPerformed(LocalDateTime.now(), "총 " + trKeyList.size() + " 종목 실시간 체결 데이터 등록 완료");
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