package stock.overseas.websocket;

import org.json.simple.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.websocket.Session;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketService {

    private MessageHandler messageHandler = new MessageHandlerImpl();
    private WebSocketClient client = new WebSocketClient();

    public WebSocketService() {
    }

    public void getInfo() throws URISyntaxException {

        // get connection
        Session session = client.connect(new URI("ws://ops.koreainvestment.com:21000"));

        client.addMessageHandler(messageHandler);

        // send message
        client.sendMessage("DNASTSLA");
        client.sendMessage("DNASTQQQ");
        client.sendMessage("DAMSSPY");
    }

    public String sendMessage(String trKey) {

        String approvalKey = getApprovalKey();

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

    private String getApprovalKey() {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        body.put("grant_type", "client_credentials");
        body.put("appkey", "PSwyURkfTGAFKqByG3jvatxtb5vbFJZhsM0Q");
        body.put("secretkey", "RQvCEwJnKvqPoEPyXHz0PKND1dR5feueXjymZ/Go7kOT2lt3tzqiIo10UFdfCWwt3xcOqhKkHb4S7Stg8fzgRNNu2BFqZWYwsJs8XtMUiyLp2vEKlYe2i9sfSAGIn4d4TTaeb/ocY6dDVBfp4HpheMx+T6JpAkhMKdhguOJREYyZbMTO6c0=");

        HttpEntity<JSONObject> entity = new HttpEntity<>(body, headers);

        UriComponents uri = UriComponentsBuilder
                .fromHttpUrl("https://openapi.koreainvestment.com:9443/oauth2/Approval")
                .build();

        ResponseEntity<JSONObject> response = restTemplate.postForEntity(uri.toString(), entity, JSONObject.class);
        JSONObject json = response.getBody();
        String approval_key = (String) json.get("approval_key");
        return approval_key;
    }
}
