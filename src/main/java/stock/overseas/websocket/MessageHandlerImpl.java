package stock.overseas.websocket;

import stock.overseas.directory.DirectoryService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MessageHandlerImpl implements MessageHandler {

    private Map<String, StockFile> stockFiles;
    private DirectoryService directoryService;

    public MessageHandlerImpl(List<String> trKeyList) {
        directoryService = new DirectoryService();
        stockFiles = directoryService.getStockFileMap(trKeyList);
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
            throw new IOException(e);
        } finally {
            writer.close();
        }
    }
}
