package ru.itone.illya4gurenko.exception;

/**
 * Исключение, выбрасываемое при передаче в Visitor объекта неподдерживаемого типа.
 */
public class VisitorTypeException extends RuntimeException {

    public VisitorTypeException(String message) {
        super(message);
    }

    public VisitorTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
