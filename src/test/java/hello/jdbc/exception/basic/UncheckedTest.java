package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class UncheckedTest {

    @Test
    void unchecked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void unchecked_throw() {
        Service service = new Service();
        assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyUncheckedException.class);
    }

    /**
     * RuntimeException 을 상속받으면 언체크(런타임) 예외가 된다.
     */
    static class MyUncheckedException extends RuntimeException {
        public MyUncheckedException(String message) {
            super(message);
        }
    }

    /**
     * UnChecked Exception 은 예외를 catch 또는 throw 하지않아도 된다.
     * 자동으로 밖으로 던져진다
     */
    static class Service {

        Repository repository = new Repository();

        /**
         * 필요한 경우에는 catch 로 처리하면 된다.
         */
        public void callCatch() {
            try {
                // 처리중 예외발생으로 throw MyUncheckedException
                repository.call();
            } catch (MyUncheckedException e) { // 필요하면 catch 하면된다.
                // 예외 처리 로직
                log.error("예외 처리, message={}", e.getMessage(), e);
            }
        }

        /**
         * 예외를 catch 또는 throws 하지 않아도 상위 메소드로 던져진다.
         */
        public void callThrow() {
            repository.call();
            // 런타임에러는 컴파일 에러가 발생하지 않는다.
            // 물론 throws MyUncheckedException 을 선언해도 된다.
            // 중요한 예외인경우 생략하지않고 선언하여 개발자가 중요한 예외임을 인지할 수 있게 하기도 한다.
        }
    }
    // 런타임(언체크) 예외 장단점
    // 런타임예외는 예외를 던지는 throws ... 를 생략할 수 있다.
    // 장점 : 신경쓰고 싶지않은 예외틑 생략가능하여 의존관계 참조에서 보다 자유롭다.
    // 단점 : catch 또는 throws 하지 않아도 되므로 개발자가 누락할 위험이 있다.

    static class Repository {
        public void call() {
            throw new MyUncheckedException("ex");
        }
    }

}
