package stock.overseas.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Settings {

    String websocketAccessKeyUrl;
    String overseasStockQuoteUrl;
    Boolean enableDebugLog;
}