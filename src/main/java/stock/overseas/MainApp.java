package stock.overseas;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import stock.overseas.directory.DirectoryServiceImpl;
import stock.overseas.domain.AuthenticationInfo;
import stock.overseas.domain.Stock;
import stock.overseas.http.HttpService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@EnableScheduling
public class MainApp {

    public static void main(String[] args) {

        HttpService httpService = new HttpService();
        DirectoryServiceImpl directoryService = new DirectoryServiceImpl();

        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        List<Stock> stockInfoList = new ArrayList<>();
        if (!directoryService.getInfoFromJsonFile(authenticationInfo, stockInfoList)) {
            return;
        }

        String approvalKey = null;
        try {
            approvalKey = httpService.getApprovalKey(authenticationInfo);
        } catch (Exception e) {
            return;
        }

        List<String> trKeyList = stockInfoList.stream()
                .map(stock -> stock.getTrKey())
                .collect(Collectors.toList());

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
