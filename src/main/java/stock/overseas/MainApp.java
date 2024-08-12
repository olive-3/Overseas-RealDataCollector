package stock.overseas;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;
import stock.overseas.calculator.USStockMarketHolidayCalculator;
import stock.overseas.directory.DirectoryServiceImpl;
import stock.overseas.domain.Authentication;
import stock.overseas.domain.Settings;
import stock.overseas.domain.Stock;
import stock.overseas.http.HttpService;
import stock.overseas.websocket.WebSocketClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@EnableScheduling
public class MainApp {

    public static void main(String[] args) throws InterruptedException {

        USStockMarketHolidayCalculator stockMarketHolidayCalculator = new USStockMarketHolidayCalculator();
        String message = stockMarketHolidayCalculator.checkWeekendOrHoliday();
        if(StringUtils.hasText(message)) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), message);
            return;
        }

        List<Stock> stocks = new ArrayList<>();
        Authentication authentication = new Authentication();
        Settings settings = new Settings();
        DirectoryServiceImpl directoryService = new DirectoryServiceImpl();
        if (!directoryService.getInfoFromJsonFile(authentication, stocks, settings)) {
            return;
        }

        //웹소켓 접속키 발급
        String approvalKey = null;
        HttpService httpService = new HttpService(settings.getWebsocketAccessKeyUrl());
        try {
            approvalKey = httpService.getApprovalKey(authentication);
        } catch (Exception e) {
            return;
        }

        //WebSocket 연결
        WebSocketClient webSocketClient;
        try {
            webSocketClient = new WebSocketClient(approvalKey, stocks, settings.getOverseasStockQuoteUrl(), settings.getEnableDebugLog());
            webSocketClient.connect();
        } catch (Exception e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "프로그램 종료");
            return;
        }

        while(true) {
            Thread.sleep(100000000);
        }
    }
}
