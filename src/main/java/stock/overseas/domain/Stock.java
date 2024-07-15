package stock.overseas.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Stock {

    private String symbol;
    private String stockName;
    private String trKey;
}