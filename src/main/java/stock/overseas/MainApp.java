package stock.overseas;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;
import stock.overseas.directory.DirectoryServiceImpl;
import stock.overseas.domain.AuthenticationInfo;
import stock.overseas.domain.Stock;
import stock.overseas.http.HttpService;
import stock.overseas.websocket.WebSocketClient;

import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@EnableScheduling
public class MainApp {

    public static void main(String[] args) throws ParseException {

//        List<String> trKeyList = new ArrayList<>();
        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        List<Stock> stockInfoList = new ArrayList<>();

        HttpService httpService = new HttpService();
        DirectoryServiceImpl directoryService = new DirectoryServiceImpl();

        if(directoryService.getInfoFromJsonFile(authenticationInfo, stockInfoList) == false) {
            return;
        }

        String approvalKey = httpService.getApprovalKey(authenticationInfo);
        if(StringUtils.hasText(approvalKey) == false) {
            return;
        }

        //        for (Stock stock : stockInfoList) {
//            trKeyList.add(stock.getTrKey());
//        }

        //폴더, 파일 생성
//        directoryService.checkDirectoryExist(trKeyList);
//        directoryService.makeFiles(trKeyList);
//
//        //WebSocket 연결
//        WebSocketClient webSocketClient;
//        try {
//            webSocketClient = new WebSocketClient(approvalKey, stockInfoList, trKeyList);
//            webSocketClient.connect(new URI("ws://ops.koreainvestment.com:21000"));
//        } catch (Exception e) {
//            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "프로그램 종료");
//            return;
//        }
//
//        while(true) {
//            try {
//                Session session = webSocketClient.getUserSession();
//                if (session != null) {
//                    webSocketClient.sendPong();
//                }
//
//                Thread.sleep(100000);   //100초
//            } catch (Exception e) {
//                log.info("{}", e.getMessage());
//            }
//        }
    }
}
