package stock.overseas;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import stock.overseas.websocket.WebSocketService;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class OverseasApplication {

	public static void main(String[] args) throws URISyntaxException, IOException, ParseException {
		ConfigurableApplicationContext context = SpringApplication.run(OverseasApplication.class, args);

		List<String> trKeyList = getTrKey();
		WebSocketService service = new WebSocketService(trKeyList);
		service.getInfo();
	}

	private static List<String> getTrKey() throws IOException, ParseException {

		List<String> trKeyList = new ArrayList<>();
		Map<String, String> marketMap = new HashMap<>();
		marketMap.put("AMEX", "AMS");
		marketMap.put("NASDAQ", "NAS");
		marketMap.put("NYSE", "NYS");

		String absolutePath = Paths.get("").toAbsolutePath().toString();
		String path = absolutePath + File.separator + "RealDataCollector.json";

		Reader reader = new FileReader(path);
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject)parser.parse(reader);
		JSONObject stocks = (JSONObject)jsonObject.get("Stocks");

		for (String key : marketMap.keySet()) {
			Object market = stocks.get(key);
			if(market != null) {
				JSONArray marketArray = (JSONArray)stocks.get(key);
				for (Object arr : marketArray) {
					Object symbol = ((JSONObject) arr).get("Symbol");
					trKeyList.add("D" + marketMap.get(key) + symbol.toString());
				}
			}
		}

		return trKeyList;
	}

}
