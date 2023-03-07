package stock.overseas.directory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import stock.overseas.gui.MyGUI;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DirectoryService {

    private String programPath = Paths.get("").toAbsolutePath().toString();
    private String jsonPath = programPath + File.separator + "RealDataCollector.json";
    private String realDataPath = programPath + File.separator + "RealData";
    private MyGUI myGUI = MyGUI.getInstance();

    public void checkJsonFileExist() throws FileNotFoundException {

        File jsonFile = new File(jsonPath);
        if (!jsonFile.exists()) {
            throw new FileNotFoundException();
        }
    }

    public void checkJsonFileForm() throws IOException, ParseException {

        Reader reader = new FileReader(jsonPath);

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(reader);   // throw IOException, ParseException
        JSONObject authentication = (JSONObject) jsonObject.get("Authentication");

        List<String> jsonAuthKeyList = Arrays.asList("GrantType", "AppKey", "SecretKey");
        for (String authKey : jsonAuthKeyList) {
            String authValue = authentication.get(authKey).toString();
            if (authValue.isEmpty()) {
                String errorMessage = "인증 관련 " + authKey + " 값이 존재 하지 않아 인증을 진행 할 수 없습니다. 해당 값을 설정 후 다시 실행해 주시기 바랍니다.";
                myGUI.actionPerformed(LocalDateTime.now(), errorMessage);
            }
        }
    }

    public List<String> getTrKeyList() throws IOException, ParseException {

        List<String> trKeyList = new ArrayList<>();
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
                    Object symbol = ((JSONObject) arr).get("Symbol");
                    trKeyList.add("D" + marketMap.get(key) + symbol.toString());
                }
            }
        }

        return trKeyList;
    }

    public void checkDirectoryExist(List<String> trKeyList) {

        makeRealDataDirectory();
        for (String trKey : trKeyList) {
            makeTickerAndYearDirectory(trKey);
        }
    }

    private void makeRealDataDirectory() {

        File folder = new File(realDataPath);

        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    private void makeTickerAndYearDirectory(String trKey) {

        int year = LocalDateTime.now().getYear();
        String stockName = trKey.substring(4);
        String pathTickerYear = realDataPath + File.separator + stockName + File.separator + year;
        File folder = new File(pathTickerYear);

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
                    myGUI.actionPerformed(LocalDateTime.now(), "파일 생성 중 오류 발생");
                }
            }
        }
    }
    private String getPath(String key) {

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).substring(2, 8);
        int year = LocalDateTime.now().getYear();
        String filePath = realDataPath + File.separator + key + File.separator + year + File.separator + key + "_" + date + ".txt";

        return filePath;
    }
}
