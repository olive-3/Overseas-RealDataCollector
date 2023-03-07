package stock.overseas.websocket;

import java.io.File;

public class StockFile {

    private String stockName;
    private File file;
    private long sequence;

    public StockFile(String stockName, File file, long sequence) {
        this.stockName = stockName;
        this.file = file;
        this.sequence = sequence;
    }

    public String getStockName() {
        return stockName;
    }

    public File getFile() {
        return file;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }
}
