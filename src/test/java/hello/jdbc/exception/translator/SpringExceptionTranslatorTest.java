package hello.jdbc.exception.translator;

import hello.jdbc.connection.ConnectionConstant;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConstant.*;
import static org.assertj.core.api.Assertions.*;

@Slf4j
public class SpringExceptionTranslatorTest {

    DataSource dataSource;

    @BeforeEach
    void init() {
        dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    }

    @Test
    void sqlExceptionErrorCode() {
        // given
        String sql = "select bad grammar";
        // when
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeQuery();
        } catch (SQLException e) {
            // then
            assertThat(e.getErrorCode()).isEqualTo(42122);
            int errorCode = e.getErrorCode();
            log.info("errorCode = {}", errorCode);
            // org.h2.jdbc.JdbcSQLSyntaxErrorException
            log.info("error", e);
            // 이렇게 직접 예외를 확인하고 하나하나 변환하는 것은 현실성이 없다.
            // DB 마다 에러코드가 다르고, 모든 예외를 이런식으로 처리할 수는 없다.
            // 그래서 스프링은 예외 변환기를 제공한다.
        }
    }

    @Test
    void exceptionTranslator() {
        // given
        String sql = "select bad grammar";
        // when
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeQuery();
        } catch (SQLException e) {
            // then
            assertThat(e.getErrorCode()).isEqualTo(42122);
            // org.springframework.jdbc.support.sql-error-codes.xml

            // 스프링이 제공하는 SQL 예외 변환기 사용
            SQLExceptionTranslator exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
            // org.springframework.jdbc.BadSqlGrammarException
            DataAccessException resultException = exceptionTranslator.translate("select", sql, e);
            // translator.translate(읽을수 있는 설명, 실행한 sql, 발생된 SQLException)
            // -> 적절한 스프링 데이터 접근 계층의 예외로 변환해준다.

            log.info("resultException", resultException);

            // Syntax 에러이므로 BadSqlGrammarException 이 반환된다.
            // 눈에 보이는 타입은 최상위 타입인 DataAccessException 이지만 하위를 확인해보면 BadSqlGrammarException 이다
            assertThat(resultException.getClass()).isEqualTo(BadSqlGrammarException.class);
        }

    }
}
// sql-error-codes.xml 파일을 확인하면 알 수 있다.
// org.springframework.jdbc.support.sql-error-codes.xml
// 스프링 SQL 예외 변환기는 SQL ErrorCode 를 이 파일에 대입하여 어떤 스프링 데이터 접근 예외로 전환할지 찾아낸다.
// 예를들어 H2 데이터베이스에서 42000 이 발생하면 badSqlGrammarCodes 이기 때문에 BadSqlGrammarException 을 반환한다.

// 정리
// 스프링은 데이터 접근 계층에 대한 일관된 예외 추상화를 제공한다.
// 스프링은 예외 변환기를 통해서 SQLException 의 ErrorCode 에 맞는 적절한 스프링 데이터 접근 예외로 변환해준다.
// 만약 서비스, 컨트롤러 계층에서 예외 처리가 필요하면 특정 기술에 종속적인 SQLException 같은 예외를 직접 사용하는 것이 아니라,
// 스프링이 제공하는 데이터 접근 예외를 사용하면 된다.
// 스프링 예외 추상화와 더불어 특정 기술에 종속적이지 않게 되었다. 기술이 변경되어도 코드 변경을 최소화할 수 있다.
// 물론 스프링이 제공하는 예외를 사용하기 때문에, 스프링에대한 기술 종속성은 발생한다.