package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;

/**
 * 체크 예외의 문제점 확인
 */
@Slf4j
public class CheckedAppTest {

    @Test
    void checked() {
        Controller controller = new Controller();
        assertThatThrownBy(() -> controller.request())
                .isInstanceOf(Exception.class);
    }

    static class Controller {
        Service service = new Service();

        public void request() throws SQLException, ConnectException {
            // 서비스 로직에서 던져진 체크예외를 처리할 수 없어 throws
            service.logic();
        }
    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() throws SQLException, ConnectException {
            // 호출한 메소드에서 던져진 체크예외를 처리할 수 없어 throws
            repository.call();
            networkClient.call();
            // 2가지 문제가 발생한다
            // 1. 복구 불가능한 예외
            // 2. 의존관계에 대한 문제 발생
        }
    }

    static class NetworkClient {
        public void call() throws ConnectException {
            // 체크예외 throw
            throw new ConnectException("Connect Failed");
        }
    }

    static class Repository {
        public void call() throws SQLException {
            // 체크예외 throw
            throw new SQLException("ex");
        }
    }

}
/* 체크 예외 활용 */
// 기본적으로 런타임(언체크) 예외를 사용하자
// 체크예외는 비즈니스로직상 의도적으로 던져야 하는 예외에만 사용하자.
// -> 예) 계좌이체 실패 예외, 결제시 포인트부족 예외, ID/PW 불일치 예외 등
// 물론, 런타임에러로 처리해도 되지만, 개발자가 실수로 누락하지 않게하기위해 체크에러를 사용할 수 있다.

/* 체크예외의 문제점 */
// 1. 복구 불가능한 예외
// 대부분의 예외는 복구가 불가능하다.
// SQLException 을 예를 들면, DB 에 문제가 발생했을 때 발생하는 Exception 이다.
// SQL 문법, DB 자체의 문제 , DB 서버 문제등이 있다. 이런 문제들은 복구가 불가능하다.
// 대부분의 서비스나 컨트롤러에서 이런 문제를 해결할 수 없다. 따라서 일관성있게 공통으로 처리해야한다.
// 에러 로그를 남기고 개발자가 해당 오류를 빠르게 인지하는 것이 중요하다.
// 서블릿 필터, 스프링 인터셉터, 스프링의 ControllerAdvice 를 이용하면 공통으로 해결 가능하다.
// 2. 의존관계에 대한 문제
// 체크예외이기 때문에, 컨트롤러나 서비스 입장에서 throws 를 선언할 수 밖에 없다.
// 그러면 컨트롤러나 서비스에서 java.sql.SQLException 을 의존하게 되어 문제가 된다.
// 향후 JDBC 기술을 변경하게 되어 JPAException 으로 예외가 변경된다면 컨트롤러, 서비스도 변경이 불가피해진다.
// 이는 OCP, DI를 통해 클라이언트 코드의 변경없이 구현체를 바꿀수 있는 장점이 없어지게 된다.

// 정리
// 처리할 수 있는 체크예외라면 서비스, 컨트롤러에서 처리하겠지만, DB 나 네트워크통신 처럼 시스템 레벨에서 발생하여
// 던져진 예외들은 대부분 복구가 불가능하다. 문제는 서비스, 컨트롤러에서 불필요한 의존관계가 발생한다는 것이다.

// throws Exception 에 대하여
// void method() throws SQLException, ConnectException {...}
// 위의 선언을 아래로 바꾸면?
// void method() throws Exception {...}
// Exception 의 하위타입(자식)인 SQLException, ConnectException 도 던져진다.
// 그러나, Exception 은 최상위 타입이므로 다른 예외도 던져지는 문제가 발생한다.
// 모든 예외가 다 던져져 컴파일 에러가 발생하지 않아 잡아야하는 에러도 놓칠 위험이 있다.

