package stock.overseas.websocket;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessageHandlerImpl implements MessageHandler {

    private File fileTSLA = new File(".");
    private File fileTQQQ = new File(".");
    private File fileSPY = new File(".");
    private long sequenceTSLA = 0L;
    private long sequenceTQQQ = 0L;
    private long sequenceSPY = 0L;

    public MessageHandlerImpl() {

        String path1 = getPath("TSLA", fileTSLA);
        String path2 = getPath("TQQQ", fileTQQQ);
        String path3 = getPath("SPY", fileSPY);

        fileTSLA = new File(path1);
        fileTQQQ = new File(path2);
        fileSPY = new File(path3);

        System.out.println("path1 = " + path1);

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

    private String getPath(String key, File file) {

        LocalDateTime now;
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).substring(2, 8);

        String absolutePath = file.getAbsolutePath();
        absolutePath = absolutePath.substring(0, absolutePath.length()-1);

        String path = absolutePath + "logs" + File.separator + key + File.separator + key + "_" + date + ".txt";

        return path;
    }
}
