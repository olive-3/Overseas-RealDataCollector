package stock.overseas;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;
import stock.overseas.directory.DirectoryServiceImpl;
import stock.overseas.domain.AuthenticationInfo;
import stock.overseas.domain.Stock;
import stock.overseas.http.HttpService;
import stock.overseas.websocket.WebSocketClient;

import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@EnableScheduling
public class MainApp {

    public static void main(String[] args) {

        String message = checkWeekendOrHoliday();
        if(StringUtils.hasText(message)) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), message);
        }

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

        //WebSocket 연결
        WebSocketClient webSocketClient = new WebSocketClient(approvalKey, stockInfoList);
        try {
            webSocketClient.connect(new URI("ws://ops.koreainvestment.com:21000"));
        } catch (Exception e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "프로그램 종료");
            return;
        }


        //폴더, 파일 생성
//        directoryService.checkDirectoryExist(trKeyList);
//        directoryService.makeFiles(trKeyList);

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

    private static String checkWeekendOrHoliday() {

        ZoneId americaZoneId = ZoneId.of("America/New_York");
        LocalDate now = LocalDate.now(americaZoneId);

        //주말
        if(now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return "오늘은 주말이어서 미국 주식 시장이 휴장합니다.";
        }

        Map<String, String> holidayMap = new HashMap<>();
        holidayMap.put("2024-01-01", "새해 첫날 (New Year's Day)");
        holidayMap.put("2024-01-15", "마틴 루터 킹 주니어 데이 (Martin Luther King Jr. Day)");
        holidayMap.put("2024-02-19", "미국 대통령의 날 (Presidents' Day)");
        holidayMap.put("2024-03-29", "성금요일 (Good Friday)");
        holidayMap.put("2024-05-27", "메모리얼 데이 (Memorial Day)");
        holidayMap.put("2024-07-04", "독립 기념일 (Independence Day)");
        holidayMap.put("2024-09-02", "노동절 (Labor Day)");
        holidayMap.put("2024-10-14", "콜럼버스 데이 (Columbus Day)");
        holidayMap.put("2024-11-11", "미국 제정 기념일 (Veterans Day)");
        holidayMap.put("2024-11-28", "추수감사절 (Thanksgiving Day)");
        holidayMap.put("2024-12-25", "크리스마스 (Christmas Day)");

        //공휴일
        for (String holiday : holidayMap.keySet()) {
            if(holiday.equals(now.toString())) {
                return "오늘은 " + holidayMap.get(holiday) + "로 미국 주식 시장이 휴장합니다.";
            }
        }

        return null;
    }
}
