package stock.overseas.directory;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import stock.overseas.domain.Stock;
import stock.overseas.domain.StockFile;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DirectoryServiceImpl implements DirectoryService {

    private String programPath = Paths.get("").toAbsolutePath().toString();
    private String jsonPath = programPath + File.separator + "RealDataCollector.json";
    private String realDataPath = programPath + File.separator + "RealData";

    public void checkJsonFileExist() throws FileNotFoundException {

        File jsonFile = new File(jsonPath);
        if (!jsonFile.exists()) {
            throw new FileNotFoundException();
        }
    }

    public void checkJsonFileForm() throws IOException, ParseException {

        Reader reader = new FileReader(jsonPath);

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(reader);
        JSONObject authentication = (JSONObject) jsonObject.get("Authentication");

        List<String> jsonAuthKeyList = Arrays.asList("GrantType", "AppKey", "SecretKey");
        for (String authKey : jsonAuthKeyList) {
            String authValue = authentication.get(authKey).toString();
            if (authValue.isEmpty()) {
                String errorMessage = "인증 관련 " + authKey + " 값이 존재 하지 않아 인증을 진행 할 수 없습니다. 해당 값을 설정 후 다시 실행해 주시기 바랍니다.";
                log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), errorMessage);
            }
        }
    }

    public void initStock(List<Stock> stockListInfo) throws IOException, ParseException {

        Map<String, String> marketMap = new HashMap<>();
        marketMap.put("NASDAQ", "NAS");
        marketMap.put("AMEX", "AMS");
        marketMap.put("NYSE", "NYS");

        Reader reader = new FileReader(jsonPath);

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(reader);
        JSONObject stocks = (JSONObject) jsonObject.get("Stocks");

        for (String key : marketMap.keySet()) {
            Object market = stocks.get(key);
            if (market != null) {
                JSONArray marketArray = (JSONArray) stocks.get(key);
                for (Object arr : marketArray) {
                    String symbol = ((JSONObject) arr).get("Symbol").toString();
                    String stockName = ((JSONObject) arr).get("Name").toString();
                    String trKey = "D" + marketMap.get(key) + symbol;

                    Stock stock = new Stock(symbol, stockName, trKey);
                    stockListInfo.add(stock);
                }
            }
        }
    }

    public Map<String, StockFile> getStockFileMap(List<String> trKeyList) {

        Map<String, StockFile> stockFiles = new ConcurrentHashMap<>();

        for (String trKey : trKeyList) {

            String stockName = trKey.substring(4);
            String path = getPath(stockName);
            File file = new File(path);

            if(!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "txt 파일 작성 중 오류 발생");
                }
            }

            StockFile stockFile = new StockFile(stockName, file, 0L);
            stockFiles.put(trKey, stockFile);
        }

        return stockFiles;
    }

    /*
     * RealData/주식명 디렉토리 존재 유무 -> 존재하지 않는 경우, 생성
     */
    public void checkDirectoryExist(List<String> trKeyList) {

        makeRealDataDirectory();
        for (String trKey : trKeyList) {
            makeTickerDirectory(trKey);
        }
    }

    private void makeRealDataDirectory() {

        File folder = new File(realDataPath);

        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    private void makeTickerDirectory(String trKey) {

        String stockName = trKey.substring(4);
        String pathTicker = realDataPath + File.separator + stockName;
        File folder = new File(pathTicker);

        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public void makeFiles(List<String> trKeyList) {

        for (String trKey : trKeyList) {
            String stockName = trKey.substring(4);
            String path = getPath(stockName);
            File file = new File(path);

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "파일 생성 중 오류 발생");
                }
            }
        }
    }

    private String getPath(String key) {

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).substring(2, 8);
        String filePath = realDataPath + File.separator + key + File.separator + key + "_" + date + ".txt";

        return filePath;
    }
}
