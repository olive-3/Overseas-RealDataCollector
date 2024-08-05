package stock.overseas;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;
import stock.overseas.calculator.USStockMarketHolidayCalculator;
import stock.overseas.directory.DirectoryServiceImpl;
import stock.overseas.domain.AuthenticationInfo;
import stock.overseas.domain.Stock;
import stock.overseas.http.HttpService;
import stock.overseas.websocket.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@EnableScheduling
public class MainApp {

    public static void main(String[] args) throws URISyntaxException {

        USStockMarketHolidayCalculator stockMarketHolidayCalculator = new USStockMarketHolidayCalculator();
        String message = stockMarketHolidayCalculator.checkWeekendOrHoliday();
        if(StringUtils.hasText(message)) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), message);
            return;
        }

        List<Stock> stockInfoList = new ArrayList<>();
        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        DirectoryServiceImpl directoryService = new DirectoryServiceImpl();
        if (!directoryService.getInfoFromJsonFile(authenticationInfo, stockInfoList)) {
            return;
        }

        String approvalKey = null;
        HttpService httpService = new HttpService(directoryService.getWebsocketAccessKeyUrl());
        try {
            approvalKey = httpService.getApprovalKey(authenticationInfo);
        } catch (Exception e) {
            return;
        }

        //WebSocket 연결
        WebSocketClient webSocketClient = new WebSocketClient(approvalKey, stockInfoList, new URI(directoryService.getOverseasStockQuoteUrl()));
        try {
            webSocketClient.connect();
        } catch (Exception e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "프로그램 종료");
            return;
        }

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
