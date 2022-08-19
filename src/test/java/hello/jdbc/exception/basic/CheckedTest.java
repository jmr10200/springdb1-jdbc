package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class CheckedTest {

    @Test
    void checked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void checked_throw() {
        Service service = new Service();
        // service.callThrow() 에서 예외를 잡지않고 throws 했으므로
        // 발생한 Exception 이 여기까지 던져진다.
        assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyCheckedException.class);
    }

    /**
     * Exception 을 상속받은 예외는 체크예외가 된다.
     * RuntimeException 을 상속받으면 언체크(런타임) 예외가 된다.
     */
    static class MyCheckedException extends Exception {
        public MyCheckedException(String message) {
            // 생성자를 통해 message 기능 그대로 이용한다.
            super(message);
        }
    }

    /**
     * Checked Exception 는 예외를 catch 또는 throw 해야한다.
     */
    private class Service {

        Repository repository = new Repository();

        /**
         * 예외를 잡아 처리하는 코드
         */
        public void callCatch() {
            try {
                // 예외 발생, throw MyCheckedException
                repository.call();
            } catch (MyCheckedException e) { // 예외 잡아서
                // 예외 처리 로직
                log.error("예외 처리, message={}", e.getMessage(), e);
                // 실행결과 확인
                // 첫번째 줄 : 예외 처리, message=ex -> 메시지가 그대로 남는다.
                // 두번째 줄부터 stack trace 출력된다. log 의 마지막 파라미터에 e 를 지정했기 때문에 출력된다.
                // 즉, 여기서 예외를 잡아서 처리한다.
            }
        }

        /**
         * 체크 예외를 잡지않고 밖으로 던지는 코드
         * 체크 예외는 예외를 잡지 않고 밖으로 던지려면 throws 예외를 필수로 선언해야 함
         */
        public void callThrow() throws MyCheckedException {
            // try ~ catch 로 예외를 잡지않고 밖으로 throws 했다.
            // 그러면, 이 callThrow() 메소드를 호출하는 곳으로 에러가 전달된다.
            repository.call();
            // 여기서 throws MyCheckedException 를 설정하지 않으면 컴파일 에러 발생한다.
            // MyCheckedException 대신 부모타입인 Exception 을 설정해도 던져진다.
        }
        // 체크 예외의 장단점
        // 체크 예외는 throws 하거나 catch 를 필수로 해야한다. 아니면 컴파일 에러가 발생한다.
        // 장점 : 개발자가 실수로 누락하지않도록 컴파일러를 통해 확인할 수 있다.
        // 단점 : 모든 체크 예외를 반드시 처리해야하므로 상당히 번거로워진다. 또한 의존관계에따른 문제도 발생할 수 있다.

    }

    static class Repository {
        public void call() throws MyCheckedException{
            throw new MyCheckedException("ex");
        }
    }

}
