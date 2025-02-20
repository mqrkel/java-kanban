package exceptions;

public class InvalidTaskTimeException extends Exception {
    public InvalidTaskTimeException(String s) {
        System.out.println(s);
    }
}
