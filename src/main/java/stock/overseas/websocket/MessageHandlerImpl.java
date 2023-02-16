package stock.overseas.websocket;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MessageHandlerImpl implements MessageHandler {

    private Map<String, StockFile> stockFiles = new ConcurrentHashMap<>();

    public MessageHandlerImpl(List<String> trKeyList) {

        for (String trKey : trKeyList) {

            String stockName = trKey.substring(4);

            File file = new File(".");
            String path = getPath(stockName, file);
            file = new File(path);

            if(!file.exists()) {
                try {
                    file.createNewFile();
                    log.info("{} file created", stockName);
                } catch (IOException e) {
                    log.info(e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            StockFile stockFile = new StockFile(stockName, file, 0L);
            stockFiles.put(trKey, stockFile);
        }
    }

    @Override
    public void handleMessage(String message) throws IOException {

        if (message.contains("header")) {   // 첫 응답과 PINGPONG 메세지 제외
            return;
        }

        String[] getData = message.split("\\^");
        String trKey = getData[0].split("\\|")[3];

        write(trKey, getData);
    }

    private String getPath(String key, File file) {

        LocalDateTime now;
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).substring(2, 8);

        String absolutePath = file.getAbsolutePath();
        absolutePath = absolutePath.substring(0, absolutePath.length()-1);

        String path = absolutePath + "logs" + File.separator + key + File.separator + key + "_" + date + ".txt";

        return path;
    }

    private void write(String trKey, String[] getData) throws IOException {

        StockFile stockFile = stockFiles.get(trKey);
        long sequence = stockFile.getSequence();
        stockFile.setSequence(++sequence);

        FileWriter fw = null;
        try {
            fw = new FileWriter(stockFile.getFile(), true);
        } catch (IOException e) {
            log.info(e.getMessage());
            throw new RuntimeException(e);
        }
        BufferedWriter writer = new BufferedWriter(fw);

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

        writer.close();
    }
}
