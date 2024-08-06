package stock.overseas.http;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import stock.overseas.domain.Authentication;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class HttpService {

    private String websocketAccessKeyUrl;

    public HttpService(String websocketAccessKeyUrl) {
        this.websocketAccessKeyUrl = websocketAccessKeyUrl;
    }

    /**
     * 실시간 (웹소켓) 접속키 발급
     */
    public String getApprovalKey(Authentication authentication) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = createHttpBody(authentication);
        HttpEntity<JSONObject> entity = new HttpEntity<>(body, headers);

        UriComponents uri = UriComponentsBuilder
                .fromHttpUrl(websocketAccessKeyUrl)
                .build();

        String approvalKey = null;
        try {
            ResponseEntity<JSONObject> response = restTemplate.postForEntity(uri.toString(), entity, JSONObject.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                JSONObject json = response.getBody();
                approvalKey = (String) json.get("approval_key");
                log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "웹소켓 토큰 발급 => 성공");
            }
        } catch (HttpClientErrorException e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "웹소켓 토큰 발급 => 실패 " + e.getStatusCode());
            throw new RuntimeException();
        }

           return approvalKey;
    }

    private JSONObject createHttpBody(Authentication authentication) {
        JSONObject body = new JSONObject();
        body.put("grant_type", authentication.getGrantType());
        body.put("appkey", authentication.getAppKey());
        body.put("secretkey", authentication.getSecretKey());
        return body;
    }
}
