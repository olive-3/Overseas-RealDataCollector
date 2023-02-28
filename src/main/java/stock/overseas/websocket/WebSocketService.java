package stock.overseas.websocket;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import stock.overseas.MyAuthenticationException;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
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

    private String getApprovalKey() throws ParseException, IOException {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = null;
        try {
            body = getJsonObject();
        } catch (MyAuthenticationException e){
            log.info("{}", e.getMessage());
            client.getUserSession().close();
            System.exit(1);
        }
        HttpEntity<JSONObject> entity = new HttpEntity<>(body, headers);

        UriComponents uri = UriComponentsBuilder
                .fromHttpUrl("https://openapi.koreainvestment.com:9443/oauth2/Approval")
                .build();

        ResponseEntity<JSONObject> response;
        try {
            response = restTemplate.postForEntity(uri.toString(), entity, JSONObject.class);
            JSONObject json = response.getBody();
            String approval_key = (String) json.get("approval_key");
            return approval_key;
        } catch(HttpClientErrorException e) {
            log.info("인증 실패");
            client.getUserSession().close();
            System.exit(1);
        }

        return "fail";  // 추후 변경 예정
    }

    private JSONObject getJsonObject() throws IOException, ParseException, MyAuthenticationException{

        String absolutePath = Paths.get("").toAbsolutePath().toString();
        String path = absolutePath + File.separator + "RealDataCollector.json";

        Reader reader = null;
        try {
            reader = new FileReader(path);
        } catch (FileNotFoundException e) {
            log.info("설정 파일 RealDataCollector.json 파일이 존재하지 않아 프로그램을 종료합니다.");
            System.exit(1);
        }

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject)parser.parse(reader);
        JSONObject authentication = (JSONObject)jsonObject.get("Authentication");
        String appKey = authentication.get("appKey").toString();
        String secretKey = authentication.get("secretKey").toString();

        if(appKey.isEmpty()) {
            throw new MyAuthenticationException("인증 관련 AppKey 값이 존재 하지 않아 인증을 진행 할 수 없습니다. 해당 값을 설정 후 다시 실행해 주시기 바랍니다.");
        }
        if(secretKey.isEmpty()) {
            throw new MyAuthenticationException("인증 관련 SecretKey 값이 존재 하지 않아 인증을 진행 할 수 없습니다. 해당 값을 설정 후 다시 실행해 주시기 바랍니다.");
        }

        JSONObject body = new JSONObject();

        body.put("grant_type", authentication.get("grantType").toString());
        body.put("appkey", appKey);
        body.put("secretkey", secretKey);
        return body;
    }
}
