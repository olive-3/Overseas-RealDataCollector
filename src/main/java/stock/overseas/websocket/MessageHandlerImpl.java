package stock.overseas.websocket;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessageHandlerImpl implements MessageHandler {

    private File fileTSLA = new File("");
    private File fileTQQQ = new File("");
    private File fileSPY = new File("");
    private long sequenceTSLA = 0L;
    private long sequenceTQQQ = 0L;
    private long sequenceSPY = 0L;
    private LocalDateTime now;
    private String date;

    public MessageHandlerImpl() {

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).substring(2, 8);

        String path1 = fileTSLA.getAbsolutePath() + "/src/main/resources/logs/TSLA/TSLA_" + date + ".txt";
        String path2 = fileTQQQ.getAbsolutePath() + "/src/main/resources/logs/TQQQ/TQQQ_" + date + ".txt";
        String path3 = fileSPY.getAbsolutePath() + "/src/main/resources/logs/SPY/SPY_" + date + ".txt";
        System.out.println("path1 = " + path1);

        fileTSLA = new File(path1);
        fileTQQQ = new File(path2);
        fileSPY = new File(path3);
        if(!fileTSLA.exists()) {
            try {
                fileTSLA.createNewFile();
                System.out.println("TSLA created");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if(!fileTQQQ.exists()) {
            try {
                fileTSLA.createNewFile();
                System.out.println("TQQQ created");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if(!fileSPY.exists()) {
            try {
                fileTSLA.createNewFile();
                System.out.println("SPY created");
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
        String[] strings = getData[0].split("\\|");
        String tr_key = strings[3].substring(4);

        FileWriter fwTSLA = new FileWriter(fileTSLA, true);
        BufferedWriter writerTSLA = new BufferedWriter(fwTSLA);

        FileWriter fwTQQQ = new FileWriter(fileTQQQ, true);
        BufferedWriter writerTQQQ = new BufferedWriter(fwTQQQ);

        FileWriter fwSPY = new FileWriter(fileSPY, true);
        BufferedWriter writerSPY = new BufferedWriter(fwSPY);

        if(tr_key.equals("TSLA")) {

            writerTSLA.write(Long.toString(++sequenceTSLA));
            writerTSLA.write(",");
            writerTSLA.write(getData[5]);   // 현지시간
            writerTSLA.write(",");
            writerTSLA.write(getData[11]);  // 현재가
            writerTSLA.write(",");
            writerTSLA.write(getData[19]);   // 체결량
            writerTSLA.write(",");
            writerTSLA.write(getData[25]);  // 시장구분
            writerTSLA.newLine();

            writerTSLA.close();
        }
        else if(tr_key.equals("TQQQ")) {

            writerTQQQ.write(Long.toString(++sequenceTQQQ));
            writerTQQQ.write(",");
            writerTQQQ.write(getData[5]);   // 현지시간
            writerTQQQ.write(",");
            writerTQQQ.write(getData[11]);  // 현재가
            writerTQQQ.write(",");
            writerTQQQ.write(getData[19]);   // 체결량
            writerTQQQ.write(",");
            writerTQQQ.write(getData[25]);  // 시장구분
            writerTQQQ.newLine();

            writerTQQQ.close();
        }
        else {
            writerSPY.write(Long.toString(++sequenceSPY));
            writerSPY.write(",");
            writerSPY.write(getData[5]);   // 현지시간
            writerSPY.write(",");
            writerSPY.write(getData[11]);  // 현재가
            writerSPY.write(",");
            writerSPY.write(getData[19]);   // 체결량
            writerSPY.write(",");
            writerSPY.write(getData[25]);  // 시장구분
            writerSPY.newLine();

            writerSPY.close();
        }

    }
}
