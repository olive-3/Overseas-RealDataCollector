package stock.overseas.directory;

import stock.overseas.domain.AuthenticationInfo;
import stock.overseas.domain.Stock;
import stock.overseas.domain.StockFile;

import java.util.List;
import java.util.Map;

public interface DirectoryService {

    boolean getInfoFromJsonFile(AuthenticationInfo authenticationInfo, List<Stock> stockListInfo);

    Map<String, StockFile> getStockFileMap(List<String> trKeyList);

    void checkDirectoryExist(List<String> trKeyList);

    void makeFiles(List<String> trKeyList);
}
