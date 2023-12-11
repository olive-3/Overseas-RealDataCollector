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
import java.util.List;
import java.util.Map;

@Slf4j
public class MessageHandler {

    private Map<String, StockFile> stockFiles;
    private DirectoryServiceImpl directoryService;
    private List<String> trKeyList = new ArrayList<>();

    public MessageHandler(List<String> trKeyList) {
        this.trKeyList = trKeyList;
    }

    public void handleMessage(String message) throws ParseException, IOException {

        //PINGPONG 메세지
        if (message.contains("PINGPONG")) {
            return;
        }

        if(message.contains("SUCCESS")) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "구독 성공");
        } else {
            handlingException(message);
        }

        //Subscribe 성공
        String[] getData = message.split("\\^");
        String trKey = getData[0].split("\\|")[3];

        write(trKey, getData);
    }

    private void handlingException(String message) throws ParseException {

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(message);
        JSONObject body  = (JSONObject) jsonObject.get("body");
        String errorMessage = body.get("msg1").toString();

        throw new IllegalArgumentException(errorMessage);
    }

    private void write(String trKey, String[] getData) throws IOException {

        stockFiles = directoryService.getStockFileMap(trKeyList);
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
            log.info("[{}] {}", LocalDateTime.now(), "파일 작성 중 오류 발생");
        } finally {
            writer.close();
        }
    }
}
