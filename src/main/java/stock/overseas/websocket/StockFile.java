package stock.overseas.websocket;

import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter @Setter
public class StockFile {

    private String stockName;
    private File file;
    private long sequence;

    public StockFile(String stockName, File file, long sequence) {
        this.stockName = stockName;
        this.file = file;
        this.sequence = sequence;
    }
}
