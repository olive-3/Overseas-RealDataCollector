package stock.overseas;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import stock.overseas.gui.MyFrame;
import stock.overseas.websocket.WebSocketService;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class OverseasApplication {

	public static void main(String[] args) throws URISyntaxException, IOException, ParseException, InterruptedException {

		SpringApplicationBuilder builder = new SpringApplicationBuilder(OverseasApplication.class);
		builder.headless(false);
		ConfigurableApplicationContext context = builder.run(args);

		List<String> trKeyList = getTrKey();
		WebSocketService service = new WebSocketService(trKeyList);
		service.getInfo();
	}

	private static List<String> getTrKey() throws IOException, ParseException, InterruptedException {

		List<String> trKeyList = new ArrayList<>();
		Map<String, String> marketMap = new HashMap<>();
		MyFrame myFrame = MyFrame.getInstance();


		String absolutePath = Paths.get("").toAbsolutePath().toString();
		String path = absolutePath + File.separator + "RealDataCollector.json";

		Reader reader = null;
		try {
			reader = new FileReader(path);
		} catch (FileNotFoundException e) {
			myFrame.actionPerformed(LocalDateTime.now(), "설정 파일 RealDataCollector.json 파일이 존재하지 않습니다.");
//			Thread.interrupted();
//			System.exit(1);
		}

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
