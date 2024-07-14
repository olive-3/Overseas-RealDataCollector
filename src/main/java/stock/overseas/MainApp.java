package stock.overseas;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.scheduling.annotation.EnableScheduling;
import stock.overseas.directory.DirectoryServiceImpl;
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

    public static void main(String[] args) {

        String approvalKey = null;
        List<String> trKeyList = new ArrayList<>();
        List<Stock> stockInfoList = new ArrayList<>();

        HttpService httpService = new HttpService();
        DirectoryServiceImpl directoryService = new DirectoryServiceImpl();

        if(directoryService.getStockListFromJsonFile(stockInfoList) == false) {
            return;
        }

        for (Stock stock : stockInfoList) {
            trKeyList.add(stock.getTrKey());
        }

        //폴더, 파일 생성
        directoryService.checkDirectoryExist(trKeyList);
        directoryService.makeFiles(trKeyList);

        //Token 발급
        try {
            approvalKey = httpService.getApprovalKey();
        } catch (IOException e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "RealDataCollector.json 파일이 존재하지 않습니다.");
            return;
        } catch (ParseException e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "RealDataCollector.json 파일 파싱 중 오류 발생");
            return;
        } catch (RuntimeException e) {
            return;
        }

        //WebSocket 연결
        WebSocketClient webSocketClient;
        try {
            webSocketClient = new WebSocketClient(approvalKey, stockInfoList, trKeyList);
            webSocketClient.connect(new URI("ws://ops.koreainvestment.com:21000"));
        } catch (Exception e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "프로그램 종료");
            return;
        }

        while(true) {
            try {
                Session session = webSocketClient.getUserSession();
                if (session != null) {
                    webSocketClient.sendPong();
                }

                Thread.sleep(100000);   //100초
            } catch (Exception e) {
                log.info("{}", e.getMessage());
            }
        }
    }
}
