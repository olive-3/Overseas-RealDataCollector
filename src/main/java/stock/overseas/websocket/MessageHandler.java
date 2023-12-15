package stock.overseas.websocket;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import stock.overseas.directory.DirectoryServiceImpl;
import stock.overseas.domain.StockFile;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MessageHandler {

    private int count;
    private int totalCount;
    private DirectoryServiceImpl directoryService = new DirectoryServiceImpl();
    private Map<String, StockFile> stockFiles = new HashMap<>();
    private List<String> trKeyList = new ArrayList<>();

    public MessageHandler(List<String> trKeyList) {
        this.trKeyList = trKeyList;
        this.count = 0;
        this.totalCount = trKeyList.size();
        this.stockFiles = directoryService.getStockFileMap(trKeyList);
    }

    public void handleMessage(String message) throws ParseException, IOException {

        log.info("{}", message);

        //PINGPONG 메세지
        if (message.contains("PINGPONG")) {
            return;
        }

        //Subscribe 메세지
        if (message.contains("msg_cd")) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(message);
            JSONObject body  = (JSONObject) jsonObject.get("body");
            String responseMessage = body.get("msg1").toString();

            if(("SUBSCRIBE SUCCESS").equals(responseMessage)) {
                JSONObject header  = (JSONObject) jsonObject.get("header");
                String symbol = header.get("tr_key").toString().substring(4);
                log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "[" + symbol + "] => 등록 성공");
                count++;

                //Subscribe 모두 성공
                if(count == totalCount) {
                    String completeMessage = "총 " + totalCount + " 종목 실시간 체결 데이터 등록 완료";
                    log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), completeMessage);
                }

                return;
            } else {
                throw new IllegalArgumentException(responseMessage);
            }
        }

        //정상 데이터
        String[] getData = message.split("\\^");
        String trKey = getData[0].split("\\|")[3];

        write(trKey, getData);
    }

    private void write(String trKey, String[] getData) throws IOException {

        StockFile stockFile = stockFiles.get(trKey);
        long sequence = stockFile.getSequence();
        stockFile.setSequence(++sequence);

        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(stockFile.getFile(), true);
            writer = new BufferedWriter(fw);

            writer.write(String.valueOf(sequence));
            writer.write(",");
            writer.write(getData[5]);   // 현지시간
            writer.write(",");
            writer.write(getData[11].replace(".", ""));  // 현재가
            writer.write(",");
            writer.write(getData[19]);   // 체결량
            writer.write(",");
            writer.write(getData[25]);  // 시장구분
            writer.newLine();
        } catch (IOException e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "파일 작성 중 오류 발생");
        } finally {
            writer.close();
        }
    }
}
