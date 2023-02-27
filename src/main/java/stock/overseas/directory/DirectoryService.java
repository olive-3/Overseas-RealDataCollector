package stock.overseas.directory;

import lombok.extern.slf4j.Slf4j;
import stock.overseas.websocket.StockFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DirectoryService {

    private Path path = Paths.get("");
    private String absolutePath = path.toAbsolutePath().toString();

    public void checkDirectoryExist(List<String> trKeyList) {

        makeRealDataDirectory();
        for (String trKey : trKeyList) {
            makeTickerAndYearDirectory(trKey);
        }
    }

    private void makeRealDataDirectory() {

        String pathRealData = absolutePath + File.separator + "RealData";
        File folder = new File(pathRealData);

        if(!folder.exists()) {
            try {
                folder.mkdir();
            }
            catch (Exception e) {
                e.getStackTrace();
            }
        }
    }

    private void makeTickerAndYearDirectory(String trKey) {

        int year = LocalDateTime.now().getYear();
        String stockName = trKey.substring(4);
        String pathTickerYear = absolutePath + File.separator + "RealData" + File.separator + stockName + File.separator + year;
        File folder = new File(pathTickerYear);

        if(!folder.exists()) {
            try {
                folder.mkdirs();
            }
            catch (Exception e) {
                e.getStackTrace();
            }
        }
    }

    public Map makeFiles(List<String> trKeyList) {

        Map<String, StockFile> stockFiles = new ConcurrentHashMap<>();

        for (String trKey : trKeyList) {

            String stockName = trKey.substring(4);

            String path = getPath(stockName);
            File file = new File(path);

            if(!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    log.info(e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            StockFile stockFile = new StockFile(stockName, file, 0L);
            stockFiles.put(trKey, stockFile);
        }

        return stockFiles;
    }

    private String getPath(String key) {

        LocalDateTime now;
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).substring(2, 8);
        int year = LocalDateTime.now().getYear();

        String filePath = absolutePath + File.separator + "RealData" + File.separator + key + File.separator + year + File.separator + key + "_" + date + ".txt";

        return filePath;
    }
}
