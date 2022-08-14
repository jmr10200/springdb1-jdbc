package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConstant.*;

@Slf4j
public class DBConnectionUtil {

    public static Connection getConnection() {
        try {
            // JDBC 가 제공하는 getConnection() 으로 Connection 취득
            // 라이브러리에 있는 데이터베이스 드라이버를 찾아서 Connection 반환
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            log.info("get connection={}, class={}", connection, connection.getClass());
            // 결과로그
            // get connection=conn0: url=jdbc:h2:tcp://localhost/~/test user=SA, class=class org.h2.jdbc.JdbcConnection
            // -> H2 커넥션 확인 가능
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
