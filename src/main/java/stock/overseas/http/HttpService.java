package stock.overseas.http;

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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
public class HttpService {

    public String getApprovalKey() throws IOException, ParseException {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = getJsonObject();
        HttpEntity<JSONObject> entity = new HttpEntity<>(body, headers);

        UriComponents uri = UriComponentsBuilder
                .fromHttpUrl("https://openapi.koreainvestment.com:9443/oauth2/Approval")
                .build();

        String approval_key = null;
        try {
            ResponseEntity<JSONObject> response = restTemplate.postForEntity(uri.toString(), entity, JSONObject.class);
            JSONObject json = response.getBody();
            approval_key = (String)json.get("approval_key");
            log.info("[{}] {}", LocalDateTime.now(), "로그온 => 성공");
        } catch (HttpClientErrorException e) {
            log.info("[{}] {}", LocalDateTime.now(), "로그온 => 실패");
        }

        return approval_key;
    }

    private JSONObject getJsonObject() throws IOException, ParseException {

        String programPath = Paths.get("").toAbsolutePath().toString();
        String jsonPath = programPath + File.separator + "RealDataCollector.json";
        Reader reader = new FileReader(jsonPath);

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(reader);   // throw IOException, ParseException
        JSONObject authentication = (JSONObject) jsonObject.get("Authentication");

        JSONObject body = new JSONObject();
        Map<String, String> jsonAuthKeyMap = Map.of("GrantType", "grant_type", "AppKey", "appkey", "SecretKey", "secretkey");
        for (String authKey : jsonAuthKeyMap.keySet()) {
            String authValue = authentication.get(authKey).toString();
            body.put(jsonAuthKeyMap.get(authKey), authValue);
        }

        return body;
    }
}
