package stock.overseas.directory;

import stock.overseas.domain.Authentication;
import stock.overseas.domain.Settings;
import stock.overseas.domain.Stock;

import java.util.List;

public interface DirectoryService {

    boolean getInfoFromJsonFile(Authentication authentication, List<Stock> stocks, Settings settings);

    void stockRealDataLogFileExists(String ticker);

    void logFileExists();
}
