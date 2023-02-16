package stock.overseas;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import stock.overseas.websocket.WebSocketService;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class OverseasApplication {

	public static void main(String[] args) throws URISyntaxException, IOException, ParseException {
		SpringApplication.run(OverseasApplication.class, args);

		List<String> trKeyList = getTrKey();
		WebSocketService service = new WebSocketService(trKeyList);
		service.getInfo();
	}

	private static List<String> getTrKey() throws IOException, ParseException {

		List<String> trKeyList;
		JSONParser parser = new JSONParser();

		File file = new File(".");
		String absolutePath = file.getAbsolutePath();
		absolutePath = absolutePath.substring(0, absolutePath.length()-1);
		String path = absolutePath + "logs" + File.separator + "TRKeyList.json";

		Reader reader = new FileReader(path);
		JSONObject jsonObject = (JSONObject)parser.parse(reader);
		trKeyList = (List<String>) jsonObject.get("TRKey");

		return trKeyList;
	}

}
