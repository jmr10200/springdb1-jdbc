package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;

/**
 * 런타임 예외 활용
 */
@Slf4j
public class UncheckedAppTest {

    @Test
    void unchecked() {
        Controller controller = new Controller();
        assertThatThrownBy(() -> controller.request())
                .isInstanceOf(RuntimeException.class);
//                .isInstanceOf(Exception.class);

    }

    @Test
    void printException() {
        Controller controller = new Controller();
        try {
            controller.request();
        } catch (Exception e) {
            // 예외를 전환할 때는 꼭! 기존 예외를 포함해야 로그에서 확인 할 수 있다.
            log.error("ex", e);
            // log 파라미터의 마지막에 예외 파라미터(여기서는 e) 를 넘기면 stacktrace 출력된다.
            // 참고 : System.out 에 출력은 e.printStackTrace() 를 사용하자
        }
    }

    static class Controller {
        Service service = new Service();

        public void request() {
            // 컨트롤러에서 throws 로 체크예외를 던지지 않아도 된다. -> 의존 X
            service.logic();
        }
    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() {
            // 서비스에서 throws 로 체크예외를 던지지 않아도 된다 -> 의존 x
            repository.call();
            networkClient.call();
        }
    }

    static class Repository {
        public void call() {
            try {
                runSQL();
            } catch (SQLException e) {
                // 예외전환 : 체크예외를 잡아서 런타임으로 throw
                throw new RuntimeSQLException(e);
                // 참고로 e 를 파라미터로 던져야 한다!
                // 런타임으로 전환할때 기존 예외를 포함시켜줘야 예외를 출력할때 stacktrace 에서 확인할 수 있다.
            }
        }

        private void runSQL() throws SQLException {
            throw new SQLException("ex");
        }
    }

    static class NetworkClient {
        public void call() {
            throw new RuntimeConnectException("Connect Failed");
        }
    }

    static class RuntimeConnectException extends RuntimeException {
        public RuntimeConnectException(String message) {
            super(message);
        }
    }

    static class RuntimeSQLException extends RuntimeException {
        public RuntimeSQLException() {
        }

        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }
    }
}

// 런타임 예외를 사용하면 중간에 DB 기술등이 변경되도 컨트롤러, 서비스는 해당 예외에 의존하지 않으므로 코드 변경하지 않아도 된다.
// 구현 기술이 변경되는 경우, 예외를 공통으로 처리하는 곳에서는 예외처리가 필요할 수 있지만,
// 공통 처리는 한곳만 변경하면 되기 때문에 변경의 영향이 최소화 된다.

// 정리
// 처음 java 를 설계할 당시는 체크예외가 더 나은 선택이라 생각했다. 그래서 기본 기능들에는 체크예외가 많다.
// 시간이 흐를수록 예외가 너무 많아져 throws 에 선언해야할 예외가 점차 늘어났다.
// 이러한 문제점 때문에 최근 라이브러리는 대부분 런타임 예외를 기본으로 제공한다.
// 런타임예외도 필요시 잡을 수 있으므로 필요에따라 잡아서 처리하고 아니면 던지게 둬서 공통으로 처리한다.
// 런타임 예외는 놓칠 수 있기 때문에 문서화가 중요하다.
// 주석으로 문서화를 잘 하거나, 코드에 throws XXXRuntimeException 를 남겨 중요성을 인지시킨다.
// 스프링에서, 주석으로 문서화 해놓고 동시에 코드에도 throws XXXException 을 선언해둔것을 볼 수 있다.
