package Exceptions;

public class NomeInvalidoException extends RuntimeException {
    public NomeInvalidoException(String message) {
        super(message);
    }
}
