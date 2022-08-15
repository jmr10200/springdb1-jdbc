package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConstant.*;

@Slf4j
public class ConnectionTest {

    @Test
    void driverManager() throws SQLException {
        // DriverManager 로 커넥션 획득
        // 커넥션 획득할 때마다 URL, USERNAME, PASSWORD 가 파라미터로 전달되야 한다.
        Connection conn1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection conn2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);

        // 각각 다른 커넥션
        log.info("connection={}, class={}", conn1, conn1.getClass());
        log.info("connection={}, class={}", conn2, conn2.getClass());
    }

    @Test
    void dataSourceDriverManager() throws SQLException {
        // DriverManagerDataSource - 항상 새로운 커넥션 획득
        // DriverManager 도 DataSource 를 통해 사용할 수 있도록 스프링이 제공하는 클래스
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        useDataSource(dataSource);
        // DriverManagerDataSource 는 DataSource 를 통해서 커넥션을 획득할 수 있다. (스프링 제공)

    }

    private void useDataSource(DataSource dataSource) throws SQLException {
        // DataSource 로 커넥션 획득
        // 처음 DataSource 객체를 생성할 때만 파라미터를 넘긴다.
        Connection conn1 = dataSource.getConnection();
        Connection conn2 = dataSource.getConnection();

        log.info("connection={}, class={}", conn1, conn1.getClass());
        log.info("connection={}, class={}", conn2, conn2.getClass());
    }

    @Test
    void dataSourceConnectionPool() throws SQLException, InterruptedException {
        // 커넥션 풀링 : HikariProxyConnection(Proxy) -> JdbcConnection(Target)
        // HikariCP 커넥션 풀 사용 : HikariDataSource 는 DataSource 인터페이스 구현함
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10);
        dataSource.setPoolName("MyPool");

        useDataSource(dataSource);
        // 커넥션 풀에서 커넥션 생성 시간 대기
        // 커넥션 풀에서 커넥션을 생성하는 작업은 어플리케이션 실행 속도에 영향을 주지 않기 위해
        // 별도의 쓰레드에서 작동한다. 그래서 테스트가 먼저 종료되어 버릴수 있다.
        // 아래와 같이 대기시간을 주어야 쓰레드 풀에 커넥션이 생성되는 로그를 확인할 수 있다.
        Thread.sleep(1000); // 1초
    }
}
/* 설정과 사용의 분리 */
// ・설정 : DataSource 객체 생성. URL, USERNAME, PASSWORD 등.
//         한 곳에 두고 관리하는 것이 향후 변경에 유연하게 대응할 수 있다.
// ・사용 : 설정 부분은 신경쓰지 않고, DataSource 의 getConnection() 만 호출해서 사용한다.

// 필요한 데이터를 DataSource 가 만들어지는 시점에 미리 다 설정하게 되면,
// 사용하는 곳에서는 접속정보 등에 의존하지 않게 된다.
// 즉, Repository 는 DataSource 만 의존하고, 이런 속성들은 몰라도 된다.
// 덕분에 객체를 설정하는 부분과, 사용하는 부분을 더 명확히 분리할 수 있다.

/** 커넥션풀 ConnectionPool 과 데이터소스 DataSource 이해 */
// 커넥션 풀
// 데이터베이스 커넥션을 획득할 때는 복잡한 과정을 거친다.
// 0. 요청 -> 1. 로직은 DB 드라이버로 커넥션 조회 -> 2. DB 드라이버는 TCP/IP 커넥션 연결(3 way handshake) ->
// 3. ID,PW 등 부가정보 DB 전달 -> 4. DB는 인증후 내부에 세션 생성 -> 5. DB는 커넥션 생성 완료 응답 -> 6. DB 드라이버의 커넥션 객체 리턴
// -> DB 뿐만아니라 TCP/IP 커넥션 생성 위한 리로스 사용등 응답속도에 영향을 주게 된다.
// 이러한 문제를 해결하는 아이디어가 커넥션풀이다.
// 커넥션을 미리 생성해두고 풀에 보관해둔 다음, 필요할 때 꺼내 쓴다. (기본값으로 보통 10개)
// 커넥션 풀 사용 1
// DB 드라이버를 통해 생성하는 것이 아니다. 이미 생성되어있는 커넥션을 객체 참조로 가져다 쓴다.
// 커넥션 풀 사용 2
// 커넥션을 모두 사용하고 나면 종료하는 것이 아니라, 다시 풀에 반환한다.

// 정리
// 적적한 커넥션 풀 숫자는 서버 스펙, DB 스펙 등 환경에 따라 다르므로 성능 테스트를 통해 정한다.
// 서버당 최대 커넥션 수를 제한할 수 있어, DB에 무한 연결이 생성되는 것을 막아줘 DB 보호 효과도 있다.
// 실무상 보통 기본값을 사용한다.
// 개념적으로 단순해서 직접 구현할 수 있지만, 오픈소스 사용(commons-dbcp2, tomcat-jdbc, HikariCP 등)

// DataSource
// 커넥션을 획득하는 방법을 추상화하는 인터페이스
// 핵심 기능 : 커넥션 조회
// 커넥션을 받는 방법은 JDBC DriverManager 직접 사용, 커넥션 풀 사용 등 다양한 방법이 존재한다.
// JDBC DriverManager 를 사용하다가 커넥션 풀로 바꿀때, 어플리케이션 로직을 변경해야한다.
// 그래서, 자바는 javax.sql.DataSource 인터페이스를 제공한다.

// 정리
// 대부분의 커넥션 풀은 DataSource 인터페이스를 이미 구현해뒀기때문에, DataSource 만 의존하도록 로직을 작성하자.
// 커넥션 풀 구현기술을 변경하고 싶으면 구현체만 바꾸면 된다.
// DriverManager 는 DataSource 를 사용하지 않는다. 그래서 DataSource 를 사용하도록 변경이 있을때, 코드를 고치지 않도록
// 스프링은 DriverManager 도 DataSource 를 통해 사용할 수 있도록 DriverManagerDataSource 클래스를 제공한다.

// 커넥션 풀 사용 HikariCP
// 로그분석
// HikariConfig : HikariCP 관련 설정 확인
// MyPool connection adder : 별도의 쓰레드를 사용해서 커넥션 풀에 커넥션 추가하는 것 확인
// -> 커넥션 생성하는 것은 상대적으로 오래걸리므로, 어플리케이션 실행시 풀 채울때까지 대기하면 어플리케이션 실행시간이 늘어난다.
// -> 따라서 별도의 쓰레드를 통해 커넥션 풀을 채워야 어플리케이션 실행 시간에 영향을 주지 않는다.
// 커넥션 풀에서 커넥션 획득 : 현 테스트에서는 2개 획득해서 리턴하지는 않았으므로, 2개를 가지고만 있다.
// -> HikariPool - MyPool - After adding stats (total=10, active=2, idle=8, waiting=0)
// -> 그래서 마지막 로그에서 사용중 2개(active=2), 대기중 8개(idle=8)를 확인할 수 있다.