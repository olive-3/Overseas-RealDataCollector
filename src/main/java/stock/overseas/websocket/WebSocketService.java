package stock.overseas.websocket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

public class WebSocketService {

    private List<String> trKeyList;
    private MessageHandler messageHandler;
    private WebSocketClient client;

    public WebSocketService(List<String> trKeyList) {
        this.trKeyList = trKeyList;
        this.messageHandler = new MessageHandlerImpl(trKeyList);
        this.client = WebSocketClient.getInstance();
    }

    public void getInfo() throws URISyntaxException, IOException, ParseException {

        // connect
        client.connect(new URI("ws://ops.koreainvestment.com:21000"));

        client.addMessageHandler(messageHandler);

        // send message
        for (String trKey : trKeyList) {
            client.sendMessage(createSendMessage(trKey));
        }
    }

    private String getApprovalKey() throws IOException, ParseException {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        String absolutePath = Paths.get("").toAbsolutePath().toString();
        String path = absolutePath + File.separator + "RealDataCollector.json";
        Reader reader = new FileReader(path);
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject)parser.parse(reader);
        JSONObject authentication = (JSONObject)jsonObject.get("Authentication");

        JSONObject body = new JSONObject();
        body.put("grant_type", authentication.get("grant_type").toString());
        body.put("appkey", authentication.get("appkey").toString());
        body.put("secretkey", authentication.get("secretkey").toString());

        HttpEntity<JSONObject> entity = new HttpEntity<>(body, headers);

        UriComponents uri = UriComponentsBuilder
                .fromHttpUrl("https://openapi.koreainvestment.com:9443/oauth2/Approval")
                .build();

        ResponseEntity<JSONObject> response = restTemplate.postForEntity(uri.toString(), entity, JSONObject.class);
        JSONObject json = response.getBody();
        String approval_key = (String) json.get("approval_key");
        return approval_key;
    }

    private String createSendMessage(String trKey) throws IOException, ParseException {

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
}
