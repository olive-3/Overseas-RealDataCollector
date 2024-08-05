package stock.overseas.directory;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import stock.overseas.domain.AuthenticationInfo;
import stock.overseas.domain.Stock;
import stock.overseas.domain.StockFile;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DirectoryServiceImpl implements DirectoryService {

    private String websocketAccessKeyUrl;
    private String overseasStockQuoteUrl;
    private String programPath = Paths.get("").toAbsolutePath().toString();
    private String jsonPath = programPath + File.separator + "RealDataCollector.json";
    private String realDataPath = programPath + File.separator + "RealData";

    /**
     * RealDataCollector.json 파일에 등록된 정보 조회
     */
    public boolean getInfoFromJsonFile(AuthenticationInfo authenticationInfo, List<Stock> stockListInfo) {

        Reader reader;
        try {
            reader = new FileReader(jsonPath);
        } catch (FileNotFoundException e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "RealDataCollector.json 파일이 존재하지 않습니다.");
            return false;
        }

        JSONParser parser = new JSONParser();
        JSONObject jsonObject;
        try {
            jsonObject = (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "RealDataCollector.json 파일 파싱 중 오류가 발생했습니다.");
            return false;
        }
        JSONObject lowerJsonObject = convertKeysToLowerCase(jsonObject);

        //JSON 파일에서 인증 파트 유효성 검사
        JSONObject authentication = (JSONObject) lowerJsonObject.get("authentication");
        List<String> jsonAuthKeyList = Arrays.asList("granttype", "appkey", "secretkey");

        for (String authKey : jsonAuthKeyList) {
            String authValue = authentication.get(authKey).toString();
            if (authValue.isEmpty()) {
                String errorMessage = "인증 관련 " + authKey + "의 value 값이 존재하지 않아 인증을 진행 할 수 없습니다. 해당 값을 설정 후 다시 실행해 주시기 바랍니다.";
                log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), errorMessage);
                return false;
            }
        }

        //JSON 파일에 등록된 주식 조회
        JSONObject stocks = (JSONObject) lowerJsonObject.get("stocks");
        Map<String, String> marketMap = new HashMap<>();
        marketMap.put("nasdaq", "NAS");
        marketMap.put("amex", "AMS");
        marketMap.put("nyse", "NYS");

        for (String key : marketMap.keySet()) {
            Object market = stocks.get(key);
            if (market != null) {
                JSONArray marketArray = (JSONArray) stocks.get(key);
                for (Object arr : marketArray) {
                    String symbol = ((JSONObject) arr).get("symbol").toString();
                    String stockName = ((JSONObject) arr).get("name").toString();
                    String trKey = "D" + marketMap.get(key) + symbol;

                    Stock stock = new Stock(symbol, stockName, trKey);
                    stockListInfo.add(stock);
                }
            }
        }

        JSONObject settings = (JSONObject) lowerJsonObject.get("settings");
        this.websocketAccessKeyUrl = (String) settings.get("websocketaccesskeyurl");
        this.overseasStockQuoteUrl = (String) settings.get("overseasstockquoteurl");
        return true;
    }

    public String getWebsocketAccessKeyUrl() {
        return websocketAccessKeyUrl;
    }

    public String getOverseasStockQuoteUrl() {
        return overseasStockQuoteUrl;
    }

    /**
     *  json 파일의 모든 키를 소문자로 변환
     */
    private JSONObject convertKeysToLowerCase(JSONObject originalJson) {
        JSONObject lowerJson = new JSONObject();
        Iterator<String> keys = originalJson.keySet().iterator();
        while(keys.hasNext()) {
            String key = keys.next();
            Object value = originalJson.get(key);

            if (value instanceof JSONObject) {
                lowerJson.put(key.toLowerCase(), convertKeysToLowerCase((JSONObject) value));
            } else if (value instanceof JSONArray) {
                lowerJson.put(key.toLowerCase(), convertArrayKeysToLowerCase((JSONArray) value));
            }else {
                lowerJson.put(key.toLowerCase(), value);
            }
        }

        return lowerJson;
    }

    private JSONArray convertArrayKeysToLowerCase(JSONArray originalJsonArray) {
        JSONArray lowerJsonArray = new JSONArray();

        for (int i = 0; i < originalJsonArray.size(); i++) {
            Object element = originalJsonArray.get(i);
            if (element instanceof JSONObject) {
                lowerJsonArray.add(convertKeysToLowerCase((JSONObject) element));
            } else if (element instanceof JSONArray) {
                lowerJsonArray.add(convertArrayKeysToLowerCase((JSONArray) element));
            } else {
                lowerJsonArray.add(element);
            }
        }

        return lowerJsonArray;
    }

    public Map<String, StockFile> getStockFileMap(List<String> trKeyList) {

        Map<String, StockFile> stockFiles = new ConcurrentHashMap<>();

        for (String trKey : trKeyList) {

            String stockName = trKey.substring(4);
            String path = getPath(stockName);
            File file = new File(path);

            if (!file.exists()) {
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

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filePath = realDataPath + File.separator + key + File.separator + key + "_" + date + ".txt";

        return filePath;
    }

    public boolean isEnableDebugLog() throws IOException, ParseException {

        Reader reader = new FileReader(jsonPath);

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(reader);
        JSONObject settings = (JSONObject) jsonObject.get("Settings");

        Boolean enableDebugLog = Boolean.valueOf(settings.get("EnableDebugLog").toString().toLowerCase());
        return enableDebugLog;
    }
}
