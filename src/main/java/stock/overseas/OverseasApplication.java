package stock.overseas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import stock.overseas.websocket.WebSocketService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class OverseasApplication {

	public static void main(String[] args) throws URISyntaxException {
		SpringApplication.run(OverseasApplication.class, args);

		List<String> trKeyList = null;
		try {
			trKeyList = getTrKey();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		WebSocketService service = new WebSocketService(trKeyList);
		service.getInfo();
	}

	private static List<String> getTrKey() throws IOException {

		List<String> trKeyList = new ArrayList<>();

		File file = new File(".");
		String absolutePath = file.getAbsolutePath();
		absolutePath = absolutePath.substring(0, absolutePath.length()-1);
		String path = absolutePath + "logs" + File.separator + "trKeyList.txt";
		file = new File(path);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufReader = new BufferedReader(fileReader);
		String line = "";
		while((line = bufReader.readLine()) != null){
			trKeyList.add(line);
		}
		bufReader.close();

		return trKeyList;
	}

}
