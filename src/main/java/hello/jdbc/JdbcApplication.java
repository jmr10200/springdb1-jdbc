package hello.jdbc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JdbcApplication {

	public static void main(String[] args) {
		SpringApplication.run(JdbcApplication.class, args);
	}

}
/** JDBC (Java Database Connectivity) 표준 인터페이스 */
// 자바에서 데이터 베이스에 접속할 수 있도록 하는 자바 API
// 데이터베이스에서 자료를 쿼리하거나 업데이트하는 방법을 제공한다.
// 대표적으로 다음 3가지 기능을 표준 인터페이스로 정의해서 제공한다.
// java.sql.Connection : 연결
// java.sql.Statement : SQL 담은 내용
// java.sql.ResultSet : SQL 요청 응답
// -> 이 JDBC 인터페이스를 각 DB 벤더(회사)에서 자신의 DB에 맞도록 구현해서 라이브러리로 제공하는데, 이를 JDBC 드라이버라고 한다.

