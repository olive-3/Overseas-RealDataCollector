package stock.overseas.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class Settings {

    String websocketAccessKeyUrl;
    String overseasStockQuoteUrl;
    Boolean enableDebugLog;
    LocalTime autoClosingTime;
}