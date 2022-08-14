package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

/**
 * JDBC - DriverManager 사용
 */
@Slf4j
public class MemberRepositoryV0 {

    /**
     * SQL
     */
    public Member save(Member member) throws SQLException {
        // INSERT 문 정의
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection conn = null;
        // PreparedStatement 는 Statement 의 자식 타입이다.
        // ? 를 통한 파라미터 바인딩을 가능하게 해준다.
        // SQL Injection 공격을 예방하기위해 PreparedStatement 통해 파라미터 바인딩하는 방식을 사용해야한다.
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            // SQL 의 첫번째 ? 에 String 값 설정
            pstmt.setString(1, member.getMemberId());
            // SQL 의 두번째 ? 에 Int 값 설정
            pstmt.setInt(2, member.getMoney());
            // Statement 로 설정한 SQL 을 전달
            pstmt.executeUpdate(); // 영향받은 DB row 수 리턴
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            // 리소스 정리 : 예외 발생여부 불문하고 역순으로 정리해야 함
            close(conn, pstmt, null);
            // 리소스를 정리(close)하지 않았을때, 커넥션이 계속 유지되는 문제가 발생할 수 있다.
            // 이를 리소스 누수 라고하는데, 결과적으로 커넥션 부족으로 장애가 발생할 수 있다.
        }
    }

    /**
     * 리소스 정리
     */
    private void close(Connection conn, Statement stmt, ResultSet rs) throws SQLException {
        // 역순으로 close()
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        // Statement 는 PreparedStatement 의 부모 타입이다.
        // public interface PreparedStatement extends Statement
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.info("error", e);
            }

        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.info("error", e);
            }

        }
    }

    /**
     * Connection 취득
     */
    private Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }
}
