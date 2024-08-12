package stock.overseas.exception;

public class CustomWebsocketException extends RuntimeException {

    public CustomWebsocketException() {
    }

    public CustomWebsocketException(String message) {
        super(message);
    }

    public CustomWebsocketException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomWebsocketException(Throwable cause) {
        super(cause);
    }

    public CustomWebsocketException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
