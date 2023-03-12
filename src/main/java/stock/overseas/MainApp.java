package stock.overseas;

import org.json.simple.parser.ParseException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import stock.overseas.directory.DirectoryService;
import stock.overseas.directory.Stock;
import stock.overseas.gui.MyGUI;
import stock.overseas.http.HttpService;
import stock.overseas.websocket.WebSocketService;

import javax.websocket.DeploymentException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EnableScheduling
@SpringBootApplication
public class MainApp {

    public static void main(String[] args) {

		SpringApplicationBuilder builder = new SpringApplicationBuilder(MainApp.class);
		builder.headless(false);
		builder.run(args);

        String approvalKey = null;
        MyGUI myGUI = MyGUI.getInstance();
        DirectoryService directoryService = new DirectoryService();
        HttpService httpService = new HttpService();

        List<Stock> stockInfoList = new ArrayList<>();

        try {
            directoryService.checkJsonFileExist();
            directoryService.checkJsonFileForm();
            directoryService.initStock(stockInfoList);
        } catch (FileNotFoundException e) {
            myGUI.actionPerformed(LocalDateTime.now(), "RealDataCollector.json 파일이 존재하지 않습니다.");
        } catch (IOException | ParseException e) {
            myGUI.actionPerformed(LocalDateTime.now(), "RealDataCollector.json 파일 파싱 중 오류 발생");
        }

        List<String> trKeyList = new ArrayList<>();
        for (Stock stock : stockInfoList) {
            trKeyList.add(stock.getTrKey());
        }

        directoryService.checkDirectoryExist(trKeyList);
        directoryService.makeFiles(trKeyList);

        try {
            approvalKey = httpService.getApprovalKey();
        } catch (IOException e) {
            myGUI.actionPerformed(LocalDateTime.now(), "RealDataCollector.json 파일이 존재하지 않습니다.");
        } catch (ParseException e) {
            myGUI.actionPerformed(LocalDateTime.now(), "RealDataCollector.json 파일 파싱 중 오류 발생");
        }

        WebSocketService webSocketService = new WebSocketService(trKeyList);
        try {
            webSocketService.getConnection();
        } catch (URISyntaxException | DeploymentException | IOException e) {
            myGUI.actionPerformed(LocalDateTime.now(), "Websocket 연결 => 실패");
        }

        webSocketService.sendMessage(approvalKey, stockInfoList);
    }
}
