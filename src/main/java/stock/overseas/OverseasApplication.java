package stock.overseas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import stock.overseas.websocket.WebSocketService;

import java.net.URISyntaxException;

@SpringBootApplication
public class OverseasApplication {

	public static void main(String[] args) throws URISyntaxException {
		SpringApplication.run(OverseasApplication.class, args);

		WebSocketService service = new WebSocketService();

		service.getInfo();
	}

}
