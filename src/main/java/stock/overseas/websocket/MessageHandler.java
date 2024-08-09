package stock.overseas.websocket;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import stock.overseas.directory.DirectoryServiceImpl;
import stock.overseas.domain.StockFile;
import stock.overseas.exception.CustomWebsocketException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MessageHandler {

    private int count;
    private int totalStockCount;
    private boolean enableDebugLog;
    private DirectoryServiceImpl directoryService;
    private Map<String, StockFile> stockFiles = new HashMap<>();

    private String programPath = Paths.get("").toAbsolutePath().toString();
    private String filePath = programPath + File.separator + "Log.txt";

    public MessageHandler(List<String> trKeyList) {
        this.count = 0;
        this.totalStockCount = trKeyList.size();
        this.directoryService = new DirectoryServiceImpl();
/*        this.enableDebugLog = directoryService.isEnableDebugLog();
        if(enableDebugLog) {
            File file = new File(filePath);
            file.createNewFile();
        }*/
    }

    public void handleMessage(String message) throws ParseException, IOException {

        //전체 로그 기록
//        if(enableDebugLog) {
//            writeMessage(message);
//        }

        System.out.println("message = " + message);
        //PINGPONG 메세지
        if (message.contains("PINGPONG")) {
            return;
        }

        //Subscribe 메세지
        if (message.contains("msg_cd")) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(message);
            JSONObject body  = (JSONObject) jsonObject.get("body");
            String responseCode = body.get("msg_cd").toString();

            //OPSP0000: SUBSCRIBE SUCCESS
            if(("OPSP0000").equals(responseCode)) {
                JSONObject header  = (JSONObject) jsonObject.get("header");
                String symbol = header.get("tr_key").toString().substring(4);
                log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "[" + symbol + "] => 등록 성공");
                count++;

                //Subscribe 모두 성공
                if(count == totalStockCount) {
                    log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "총 " + totalStockCount + " 종목 실시간 체결 데이터 등록 완료");
                }
                return;
            } else {
                String errorMessage = body.get("msg1").toString();
                throw new CustomWebsocketException(errorMessage);
            }
        }

        //정상 데이터
        String[] getData = message.split("\\|");
        int dataNum = Integer.parseInt(getData[2]);
        String trKey = getData[3].split("\\^")[0];
        String ticker = trKey.substring(4);

        //폴더, 파일 존재하는지 확인
        directoryService.stockRealDataLogFileExists(ticker);

//        if(dataNum == 1) {
//            write(trKey, getData[3]);
//        }
//        else {
//            String[] stockDataList = getData[3].split(trKey);
//            for(int i = 1; i < stockDataList.length; i++) {
//                write(trKey, trKey + stockDataList[i]);
//            }
//        }
    }

    private void write(String trKey, String stockDataString) throws IOException {

        StockFile stockFile = stockFiles.get(trKey);
        long sequence = stockFile.getSequence();
        stockFile.setSequence(++sequence);

        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(stockFile.getFile(), true);
            writer = new BufferedWriter(fw);

            String[] stockData = stockDataString.split("\\^");

            writer.write(String.valueOf(sequence));
            writer.write(",");
            writer.write(stockData[5]);   // 현지시간
            writer.write(",");
            writer.write(stockData[11].replace(".", ""));  // 현재가
            writer.write(",");
            writer.write(stockData[19]);   // 체결량
            writer.write(",");
            writer.write(stockData[25]);  // 시장구분
            writer.newLine();
        } catch (IOException e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "파일 작성 중 오류 발생");
        } finally {
            writer.close();
        }
    }

    private void writeMessage(String message) throws IOException {

        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(filePath, true);
            writer = new BufferedWriter(fw);
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "메세지 파일 작성 중 오류 발생");
        } finally {
            writer.close();
        }
    }
}
