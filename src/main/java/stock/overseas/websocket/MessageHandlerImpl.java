package stock.overseas.websocket;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MessageHandlerImpl implements MessageHandler {

    private File file = new File("");
    private long sequence = 0L;

    public MessageHandlerImpl() {

        String path = file.getAbsolutePath() + "/src/main/resources/logs/TSLA/test.txt";
        file = new File(path);
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void handleMessage(String message) throws IOException {

        if (message.contains("header")) {   // 첫 응답과 PINGPONG 메세지 제외
            return;
        }

        String[] getData = message.split("\\^");

        FileWriter fw = new FileWriter(file, true);
        BufferedWriter writer = new BufferedWriter(fw);
        writer.write(Long.toString(++sequence));
        writer.write(",");
        writer.write(getData[5]);   // 현지시간
        writer.write(",");
        writer.write(getData[11]);  // 현재가
        writer.write(",");
        writer.write(getData[19]);   // 체결량
        writer.write(",");
        writer.write(getData[25]);  // 시장구분
        writer.newLine();

        writer.close();
    }
}
