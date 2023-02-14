package stock.overseas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import stock.overseas.websocket.WebSocketService;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class OverseasApplication {

	public static void main(String[] args) throws URISyntaxException {
		SpringApplication.run(OverseasApplication.class, args);

		List<String> trKeyList = new ArrayList<>(Arrays.asList("DNASTSLA", "DNASTQQQ", "DAMSSPY"));
		WebSocketService service = new WebSocketService(trKeyList);
		service.getInfo();
	}

}
