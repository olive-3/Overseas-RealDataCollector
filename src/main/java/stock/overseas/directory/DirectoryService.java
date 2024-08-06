package stock.overseas.directory;

import stock.overseas.domain.Authentication;
import stock.overseas.domain.Settings;
import stock.overseas.domain.Stock;
import stock.overseas.domain.StockFile;

import java.util.List;
import java.util.Map;

public interface DirectoryService {

    boolean getInfoFromJsonFile(Authentication authentication, List<Stock> stocks, Settings settings);

    Map<String, StockFile> getStockFileMap(List<String> trKeyList);

    void checkDirectoryExist(List<String> trKeyList);

    void makeFiles(List<String> trKeyList);
}
