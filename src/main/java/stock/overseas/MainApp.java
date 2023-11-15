package stock.overseas;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import stock.overseas.directory.DirectoryServiceImpl;
import stock.overseas.domain.Stock;
import stock.overseas.http.HttpService;
import stock.overseas.websocket.MessageHandlerImpl;
import stock.overseas.websocket.WebSocketClient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
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

        try {
            directoryService.checkJsonFileExist();
            directoryService.checkJsonFileForm();
            directoryService.initStock(stockInfoList);
        } catch (FileNotFoundException e) {
            log.info("[{}] {}", LocalDateTime.now(), "RealDataCollector.json 파일이 존재하지 않습니다.");
        } catch (IOException | ParseException e) {
            log.info("[{}] {}", LocalDateTime.now(), "RealDataCollector.json 파일 파싱 중 오류 발생");
        }

        for (Stock stock : stockInfoList) {
            trKeyList.add(stock.getTrKey());
        }

        directoryService.checkDirectoryExist(trKeyList);
        directoryService.makeFiles(trKeyList);

        try {
            approvalKey = httpService.getApprovalKey();
        } catch (IOException e) {
            log.info("[{}] {}", LocalDateTime.now(), "RealDataCollector.json 파일이 존재하지 않습니다.");
        } catch (ParseException e) {
            log.info("[{}] {}", LocalDateTime.now(), "RealDataCollector.json 파일 파싱 중 오류 발생");
        }

        WebSocketClient webSocketClient = new WebSocketClient(approvalKey, stockInfoList);
        webSocketClient.addMessageHandler(new MessageHandlerImpl(trKeyList));
        webSocketClient.connectAndSubscribe();

        try {
            while (true) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
