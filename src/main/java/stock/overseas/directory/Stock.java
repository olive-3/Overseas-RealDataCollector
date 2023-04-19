package stock.overseas.directory;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Stock {

    private String symbol;
    private String stockName;
    private String trKey;

    public Stock(String symbol, String stockName, String trKey) {
        this.symbol = symbol;
        this.stockName = stockName;
        this.trKey = trKey;
    }
}