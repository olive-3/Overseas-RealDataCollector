package stock.overseas.directory;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.util.StringUtils;
import stock.overseas.domain.Authentication;
import stock.overseas.domain.Settings;
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

    private String programPath = Paths.get("").toAbsolutePath().toString();
    private String jsonPath = programPath + File.separator + "RealDataCollector.json";
    private String realDataPath = programPath + File.separator + "RealData";

    /**
     * RealDataCollector.json 파일에 등록된 정보 조회
     */
    public boolean getInfoFromJsonFile(Authentication authentication, List<Stock> stocks, Settings settings) {

        Reader reader;
        try {
            reader = new FileReader(jsonPath);
        } catch (FileNotFoundException e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "RealDataCollector.json 파일이 존재하지 않습니다.");
            return false;
        }

        JSONObject jsonObject;
        JSONParser parser = new JSONParser();
        try {
            jsonObject = (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "RealDataCollector.json 파일 파싱 중 오류가 발생했습니다.");
            return false;
        }

        JSONObject lowerJsonObject = convertKeysToLowerCase(jsonObject);

        //인증 정보 조회
        JSONObject authenticationObject = (JSONObject) lowerJsonObject.get("authentication");
        if(!validateAuthentication(authenticationObject)) {
            return false;
        }
        authentication.setGrantType((String) authenticationObject.get("granttype"));
        authentication.setAppKey( (String) authenticationObject.get("appkey"));
        authentication.setSecretKey((String) authenticationObject.get("secretkey"));

        //주식 조회
        JSONObject stocksObject = (JSONObject) lowerJsonObject.get("stocks");
        if (stocksObject == null || stocksObject.isEmpty()) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "RealDataCollector.json 설정 파일에 Stocks 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }

        Map<String, String> stockMarketMap = createStockMarketMap();
        for (String stockMarketKey : stockMarketMap.keySet()) {
            JSONArray stockArray = (JSONArray) stocksObject.get(stockMarketKey);
            if (stockArray == null) {
                log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "RealDataCollector.json 설정 파일에 Stocks 객체의 " + stockMarketKey.toUpperCase() + " 키가 존재하지 않습니다.");
                return false;
            }

            for (int i = 0; i < stockArray.size(); i++) {
                JSONObject stockObject = (JSONObject) stockArray.get(i);
                String symbol = (String) stockObject.get("symbol");
                if (!StringUtils.hasText(symbol)) {
                    log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "RealDataCollector.json 설정 파일에 Stocks 객체의 " + stockMarketKey.toUpperCase() + " 객체 배열에 Symbol 키가 존재하지 않거나 값이 존재하지 않습니다.");
                    return false;
                }
                String name = (String) stockObject.get("name");
                if (!StringUtils.hasText(name)) {
                    log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "RealDataCollector.json 설정 파일에 Stocks 객체의 " + stockMarketKey.toUpperCase() + " 객체 배열에 Name 키가 존재하지 않거나 값이 존재하지 않습니다.");
                    return false;
                }
                String trKey = "D" + stockMarketMap.get(stockMarketKey) + symbol;
                Stock stock = new Stock(symbol, name, trKey);
                stocks.add(stock);
            }
        }

        //설정 조회
        JSONObject settingsObject = (JSONObject) lowerJsonObject.get("settings");
        if(!validateSettings(settingsObject)) {
            return false;
        }
        settings.setWebsocketAccessKeyUrl((String) settingsObject.get("websocketaccesskeyurl"));
        settings.setOverseasStockQuoteUrl((String) settingsObject.get("overseasstockquoteurl"));
        settings.setEnableDebugLog(Boolean.valueOf((String) settingsObject.get("enabledebuglog")));

        return true;
    }

    private boolean validateAuthentication(JSONObject authenticationObject) {
        if (authenticationObject == null || authenticationObject.isEmpty()) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "RealDataCollector.json 설정 파일에 Authentication 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }

        String grantType = (String) authenticationObject.get("granttype");
        if (!StringUtils.hasText(grantType)) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "RealDataCollector.json 설정 파일에 Authentication 객체의 GrantType 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }

        String appKey = (String) authenticationObject.get("appkey");
        if (!StringUtils.hasText(appKey)) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "RealDataCollector.json 설정 파일에 Authentication 객체의 AppKey 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }

        String secretKey = (String) authenticationObject.get("secretkey");
        if (!StringUtils.hasText(secretKey)) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "RealDataCollector.json 설정 파일에 Authentication 객체의 SecretKey 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }

        return true;
    }

    private boolean validateSettings(JSONObject settingsObject) {
        if (settingsObject == null || settingsObject.isEmpty()) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "RealDataCollector.json 설정 파일에 Settings 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }


        String websocketAccessKeyUrl = (String) settingsObject.get("websocketaccesskeyurl");
        if (!StringUtils.hasText(websocketAccessKeyUrl)) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "RealDataCollector.json 설정 파일에 Settings 객체의 WebsocketAccessKeyUrl 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }

        String overseasStockQuoteUrl = (String) settingsObject.get("overseasstockquoteurl");
        if (!StringUtils.hasText(overseasStockQuoteUrl)) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "RealDataCollector.json 설정 파일에 Settings 객체의 OverseasStockQuoteUrl 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }

        String enableDebugLog = (String) settingsObject.get("enabledebuglog");
        if (!StringUtils.hasText(enableDebugLog)) {
            log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "RealDataCollector.json 설정 파일에 Settings 객체의 EnableDebugLog 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }

        return true;
    }

    private Map<String, String> createStockMarketMap() {
        Map<String, String> marketMap = new HashMap<>();
        marketMap.put("nasdaq", "NAS");
        marketMap.put("amex", "AMS");
        marketMap.put("nyse", "NYS");
        return marketMap;
    }

    /**
     * json 파일의 모든 키를 소문자로 변환
     */
    private JSONObject convertKeysToLowerCase(JSONObject originalJson) {
        JSONObject lowerJson = new JSONObject();
        Iterator<String> keys = originalJson.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = originalJson.get(key);

            if (value instanceof JSONObject) {
                lowerJson.put(key.toLowerCase(), convertKeysToLowerCase((JSONObject) value));
            } else if (value instanceof JSONArray) {
                lowerJson.put(key.toLowerCase(), convertArrayKeysToLowerCase((JSONArray) value));
            } else {
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
}
