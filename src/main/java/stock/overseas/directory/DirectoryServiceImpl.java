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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class DirectoryServiceImpl implements DirectoryService {

    private String programPath = Paths.get("").toAbsolutePath().toString();
    private String jsonPath = programPath + File.separator + "RealDataCollector.json";

    /**
     * RealDataCollector.json 파일에 등록된 정보 조회
     */
    public boolean getInfoFromJsonFile(Authentication authentication, List<Stock> stocks, Settings settings) {
        Reader reader;
        try {
            reader = new FileReader(jsonPath);
        } catch (FileNotFoundException e) {
            log.error("RealDataCollector.json 설정 파일이 존재하지 않습니다.");
            return false;
        }

        JSONObject jsonObject;
        JSONParser parser = new JSONParser();
        try {
            jsonObject = (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e) {
            log.error("RealDataCollector.json 설정 파일 파싱 중 오류가 발생했습니다.");
            return false;
        }

        JSONObject lowerJsonObject = convertKeysToLowerCase(jsonObject);

        //인증 정보 조회
        JSONObject authenticationObject = (JSONObject) lowerJsonObject.get("authentication");
        if (!validateAuthentication(authenticationObject)) {
            return false;
        }
        authentication.setGrantType((String) authenticationObject.get("granttype"));
        authentication.setAppKey((String) authenticationObject.get("appkey"));
        authentication.setSecretKey((String) authenticationObject.get("secretkey"));

        //주식 조회
        JSONObject stocksObject = (JSONObject) lowerJsonObject.get("stocks");
        if (stocksObject == null || stocksObject.isEmpty()) {
            log.error("RealDataCollector.json 설정 파일에 Stocks 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }

        Map<String, String> stockMarketMap = createStockMarketMap();
        for (String stockMarketKey : stockMarketMap.keySet()) {
            JSONArray stockArray = (JSONArray) stocksObject.get(stockMarketKey);
            if (stockArray == null) {
                log.error("RealDataCollector.json 설정 파일에 Stocks 객체의 " + stockMarketKey.toUpperCase() + " 키가 존재하지 않습니다.");
                return false;
            }

            for (int i = 0; i < stockArray.size(); i++) {
                JSONObject stockObject = (JSONObject) stockArray.get(i);
                String symbol = (String) stockObject.get("symbol");
                if (!StringUtils.hasText(symbol)) {
                    log.error("RealDataCollector.json 설정 파일에 Stocks 객체의 " + stockMarketKey.toUpperCase() + " 객체 배열에 Symbol 키가 존재하지 않거나 값이 존재하지 않습니다.");
                    return false;
                }
                String trKey = "D" + stockMarketMap.get(stockMarketKey) + symbol;
                Stock stock = new Stock(symbol, trKey);
                stocks.add(stock);
            }
        }

        if(stocks.isEmpty()) {
            log.error("해외주식 실시간 지연 체결가를 조회할 주식을 등록해 주세요.");
            return false;
        }

        //설정 조회
        JSONObject settingsObject = (JSONObject) lowerJsonObject.get("settings");
        if (!validateSettings(settingsObject)) {
            return false;
        }
        settings.setWebsocketAccessKeyUrl((String) settingsObject.get("websocketaccesskeyurl"));
        settings.setOverseasStockQuoteUrl((String) settingsObject.get("overseasstockquoteurl"));
        settings.setEnableDebugLog(Boolean.valueOf((String) settingsObject.get("enabledebuglog")));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");
        settings.setAutoClosingTime(LocalTime.parse((String) settingsObject.get("autoclosingtime"), formatter));

        return true;
    }

    private boolean validateAuthentication(JSONObject authenticationObject) {
        if (authenticationObject == null || authenticationObject.isEmpty()) {
            log.error("RealDataCollector.json 설정 파일에 Authentication 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }

        String grantType = (String) authenticationObject.get("granttype");
        if (!StringUtils.hasText(grantType)) {
            log.error("RealDataCollector.json 설정 파일에 Authentication 객체의 GrantType 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }

        String appKey = (String) authenticationObject.get("appkey");
        if (!StringUtils.hasText(appKey)) {
            log.error("RealDataCollector.json 설정 파일에 Authentication 객체의 AppKey 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }

        String secretKey = (String) authenticationObject.get("secretkey");
        if (!StringUtils.hasText(secretKey)) {
            log.error("RealDataCollector.json 설정 파일에 Authentication 객체의 SecretKey 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }

        return true;
    }

    private boolean validateSettings(JSONObject settingsObject) {
        if (settingsObject == null || settingsObject.isEmpty()) {
            log.error("RealDataCollector.json 설정 파일에 Settings 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }

        String websocketAccessKeyUrl = (String) settingsObject.get("websocketaccesskeyurl");
        if (!StringUtils.hasText(websocketAccessKeyUrl)) {
            log.error("RealDataCollector.json 설정 파일에 Settings 객체의 WebsocketAccessKeyUrl 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }

        String overseasStockQuoteUrl = (String) settingsObject.get("overseasstockquoteurl");
        if (!StringUtils.hasText(overseasStockQuoteUrl)) {
            log.error("RealDataCollector.json 설정 파일에 Settings 객체의 OverseasStockQuoteUrl 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }

        String enableDebugLog = (String) settingsObject.get("enabledebuglog");
        if (!StringUtils.hasText(enableDebugLog)) {
            log.error("RealDataCollector.json 설정 파일에 Settings 객체의 EnableDebugLog 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }

        String autoClosingTimeString = (String) settingsObject.get("autoclosingtime");
        if (!StringUtils.hasText(autoClosingTimeString)) {
            log.error("RealDataCollector.json 설정 파일에 Settings 객체의 AutoClosingTime 키가 존재하지 않거나 값이 존재하지 않습니다.");
            return false;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");
        try {
            LocalTime.parse(autoClosingTimeString, formatter);
        } catch (DateTimeParseException e) {
            log.error("RealDataCollector.json 설정 파일에 Settings 객체의 AutoClosingTime 값이 유효하지 않습니다.");
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

    /**
     * RealData/ticker/yyyy/ticker_yyMMdd.txt 존재하지 않는 경우, 생성
     * <p>
     * 해외 주식 실시간지연체결가 로그 파일명은 미국 날짜 기준으로 새성됩니다.
     */
    @Override
    public void stockRealDataLogFileExists(String ticker) {
        ZoneId americaZoneId = ZoneId.of("America/New_York");
        LocalDate now = LocalDate.now(americaZoneId);
        Path folderPath = Paths.get(programPath + File.separator + "RealData" + File.separator + ticker + File.separator + now.getYear());
        Path filePath = folderPath.resolve(ticker + "_" + DateTimeFormatter.ofPattern("yyyyMMdd").format(now) + ".txt");

        try {
            //RealData/ticker/yyyy 폴더 생성
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            //해외 주식 실시간지연체결가 로그.txt 파일 생성
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            log.error("해외 주식 실시간 지연 체결가 로그 파일 폴더 생성 중 오류가 발생했습니다.");
        }
    }

    /**
     * Log/ticker/yyMMdd.txt 존재하지 않는 경우, 생성
     * <p>
     * 전체 로그 파일명은 미국 날짜 기준으로 새성됩니다.
     */
    @Override
    public void logFileExists() {
        ZoneId americaZoneId = ZoneId.of("America/New_York");
        LocalDate now = LocalDate.now(americaZoneId);
        Path folderPath = Paths.get(programPath + File.separator + "Log");
        Path filePath = folderPath.resolve(DateTimeFormatter.ofPattern("yyyyMMdd").format(now) + ".txt");

        //Log 폴더 생성
        try {
            if (!Files.exists(folderPath)) {
                Files.createDirectory(folderPath);
            }

            //전체 로그.txt 파일 생성
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            log.error("로그 파일 폴더 생성 중 오류가 발생했습니다.");
        }
    }
}
