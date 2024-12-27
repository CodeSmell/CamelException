package codesmell.exception;

@SuppressWarnings("serial")
public class FooBarException extends RuntimeException {

    public FooBarException(String message) {
        super(message);
    }
}
