package hello.jdbc.repository.exception;

public class MyDuplicateKeyException extends MyDbException {

    // 기존에 사용했던 MyDbException 을 상속받아서 생성
    // 직접 만든 것이기 때문에, JDBC 나 JPA 등 특정 기술에 종속적이지 않다.
    public MyDuplicateKeyException() {
    }

    public MyDuplicateKeyException(String message) {
        super(message);
    }

    public MyDuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDuplicateKeyException(Throwable cause) {
        super(cause);
    }
}
