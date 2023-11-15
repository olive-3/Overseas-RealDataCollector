package stock.overseas.directory;

import org.json.simple.parser.ParseException;
import stock.overseas.domain.Stock;
import stock.overseas.domain.StockFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DirectoryService {

    void checkJsonFileExist() throws FileNotFoundException;

    void checkJsonFileForm() throws IOException, ParseException;

    void initStock(List<Stock> stockListInfo) throws IOException, ParseException;

    Map<String, StockFile> getStockFileMap(List<String> trKeyList);

    void checkDirectoryExist(List<String> trKeyList);

    void makeFiles(List<String> trKeyList);
}
