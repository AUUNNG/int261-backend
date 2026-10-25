package sit.int261.int261sampleexam.exceptions;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) { super(message); }
}