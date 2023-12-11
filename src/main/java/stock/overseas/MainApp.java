package stock.overseas;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import stock.overseas.directory.DirectoryServiceImpl;
import stock.overseas.domain.Stock;
import stock.overseas.http.HttpService;
import stock.overseas.websocket.WebSocketClient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MainApp {

    public static void main(String[] args) {

        String approvalKey = null;
        List<String> trKeyList = new ArrayList<>();
        List<Stock> stockInfoList = new ArrayList<>();

        HttpService httpService = new HttpService();
        DirectoryServiceImpl directoryService = new DirectoryServiceImpl();

        //RealDataCollector.json 파일 유효성 검사
        try {
            directoryService.checkJsonFileExist();
            directoryService.checkJsonFileForm();
            directoryService.initStock(stockInfoList);
        } catch (FileNotFoundException e) {
            log.info("[{}] {}", LocalDateTime.now(), "RealDataCollector.json 파일이 존재하지 않습니다.");
            return;
        } catch (IOException | ParseException e) {
            log.info("[{}] {}", LocalDateTime.now(), "RealDataCollector.json 파일 파싱 중 오류 발생");
            return;
        }

        for (Stock stock : stockInfoList) {
            trKeyList.add(stock.getTrKey());
        }

        directoryService.checkDirectoryExist(trKeyList);
        directoryService.makeFiles(trKeyList);

        //Token 발급
        try {
            approvalKey = httpService.getApprovalKey();
        } catch (IOException e) {
            log.info("[{}] {}", LocalDateTime.now(), "RealDataCollector.json 파일이 존재하지 않습니다.");
            return;
        } catch (ParseException e) {
            log.info("[{}] {}", LocalDateTime.now(), "RealDataCollector.json 파일 파싱 중 오류 발생");
            return;
        } catch (RuntimeException e) {
            return;
        }

        //WebSocket 연결
        try {
            WebSocketClient webSocketClient = new WebSocketClient(approvalKey, stockInfoList, trKeyList);
            webSocketClient.connect(new URI("ws://ops.koreainvestment.com:21000"));
        } catch (Exception e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "프로그램 종료");
            return;
        }

        try {
            while (true) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
