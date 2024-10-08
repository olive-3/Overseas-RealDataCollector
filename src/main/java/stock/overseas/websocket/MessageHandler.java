package stock.overseas.websocket;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import stock.overseas.directory.DirectoryServiceImpl;
import stock.overseas.exception.CustomWebsocketException;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class MessageHandler {

    private int count;
    private int totalStockCount;
    private boolean enableDebugLog;
    private DirectoryServiceImpl directoryService;
    private String programPath = Paths.get("").toAbsolutePath().toString();

    public MessageHandler(List<String> trKeyList, boolean enableDebugLog) {
        this.count = 0;
        this.totalStockCount = trKeyList.size();
        this.directoryService = new DirectoryServiceImpl();
        this.enableDebugLog = enableDebugLog;
        if (enableDebugLog) {
            directoryService.logFileExists();
        }
    }

    public void handleMessage(String message) {
        //전체 로그 기록
        if (enableDebugLog) {
            writeMessage(message);
        }

        //PINGPONG 메세지
        if (message.contains("PINGPONG")) {
            return;
        }

        //Subscribe 메세지
        if (message.contains("msg_cd")) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = null;
            try {
                jsonObject = (JSONObject) parser.parse(message);
            } catch (ParseException e) {
                log.warn("응답 메세지 파싱 중 오류가 발생했습니다.");
                return;
            }
            JSONObject body = (JSONObject) jsonObject.get("body");
            String responseCode = body.get("msg_cd").toString();

            //OPSP0000: SUBSCRIBE SUCCESS
            if (("OPSP0000").equals(responseCode)) {
                JSONObject header = (JSONObject) jsonObject.get("header");
                String symbol = header.get("tr_key").toString().substring(4);
                log.info("[" + symbol + "] => 등록 성공");
                count++;

                //Subscribe 모두 성공
                if (count == totalStockCount) {
                    log.info("총 " + totalStockCount + " 종목 실시간 체결 데이터 등록 완료");
                }
                return;
            } else {
                String errorMessage = body.get("msg1").toString();
                throw new CustomWebsocketException(errorMessage);
            }
        }

        String[] getData = message.split("\\|");
        int dataNum = Integer.parseInt(getData[2]);
        String trKey = getData[3].split("\\^")[0];
        String ticker = trKey.substring(4);
        //폴더, 파일 존재하는지 확인
        directoryService.stockRealDataLogFileExists(ticker);

        //정상 데이터
        if (dataNum == 1) {
            write(ticker, getData[3]);
        } else {
            String[] stockDataList = getData[3].split(trKey);
            for (int i = 1; i < stockDataList.length; i++) {
                write(ticker, trKey + stockDataList[i]);
            }
        }
    }

    private void write(String ticker, String stockDataString) {
        ZoneId americaZoneId = ZoneId.of("America/New_York");
        LocalDate now = LocalDate.now(americaZoneId);
        String folderPath = programPath + File.separator + "RealData" + File.separator + ticker + File.separator + now.getYear();
        String filePath = folderPath + File.separator + ticker + "_" + DateTimeFormatter.ofPattern("yyyyMMdd").format(now) + ".txt";
        File file = new File(filePath);

        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(filePath, true);
            writer = new BufferedWriter(fw);
            long sequence = getLastSequence(filePath) + 1;
            String[] stockData = stockDataString.split("\\^");

            if (file.length() != 0) {
                writer.newLine();
            }

            writer.write(String.valueOf(sequence));
            writer.write(",");
            writer.write(stockData[5]);   // 현지시간
            writer.write(",");
            writer.write(stockData[11].replace(".", ""));  // 현재가
            writer.write(",");
            writer.write(stockData[19]);   // 체결량
            writer.write(",");
            writer.write(stockData[25]);  // 시장구분
        } catch (IOException e) {
            log.warn("해외 주식 실시간 체결가 파일 작성 중 오류가 발생했습니다.");
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                log.warn("파일을 닫는 중 오류가 발생했습니다.");
            }
        }
    }

    private long getLastSequence(String filePath) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r");
        long fileLength = randomAccessFile.length();

        StringBuffer lastLine = new StringBuffer();
        for (long pointer = fileLength - 1; pointer >= 0; pointer--) {
            randomAccessFile.seek(pointer);

            char c = (char) randomAccessFile.read();

            if (c == '\n') {
                break;
            }

            lastLine.insert(0, c);
        }

        if (lastLine.length() == 0) {
            return 0L;
        }

        String lastSequence = lastLine.toString().split(",")[0];
        return Long.parseLong(lastSequence);
    }

    private void writeMessage(String message) {
        ZoneId americaZoneId = ZoneId.of("America/New_York");
        LocalDate now = LocalDate.now(americaZoneId);
        String filePath = programPath + File.separator + "Log" + File.separator + DateTimeFormatter.ofPattern("yyyyMMdd").format(now) + ".txt";

        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(filePath, true);
            writer = new BufferedWriter(fw);
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            log.warn("로그 파일 작성 중 오류가 발생했습니다.");
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                log.warn("파일을 닫는 중 오류가 발생했습니다.");
            }
        }
    }
}
