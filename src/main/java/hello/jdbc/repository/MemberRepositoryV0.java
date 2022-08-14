package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - DriverManager 사용
 */
@Slf4j
public class MemberRepositoryV0 {

    /**
     * 조회
     */
    public Member findById(String memberId) throws SQLException {
        // memberId 로 조회하는 SQL 문
        String sql = "select * from member where member_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memberId);

            // 조회 실행
            rs = pstmt.executeQuery(); // 쿼리 결과를 ResultSet 으로 리턴

            // SQL 문에서 PK 로 데이터 하나만 취득하므로 while 아닌 if 문 사용한 것
            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }

        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(conn, pstmt, rs);

        }

    }

    /**
     * 등록
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

/* ResultSet 구조 */
// 보통 SELECT 쿼리의 결과가 순서대로 들어오며, 내부의 커서(cursor) 를 이용해서 데이터를 조회한다.
// rs.next() : 호출시 커서가 다음으로 이동. 최초의 커서는 데이터를 가리키고 있지않으므로 최초 한번은 호출해야 데이터를 조회할 수 있다.
//   - true : 커서의 이동 결과 데이터가 존재함
//   - false : 커서의 이동 결과 데이터가 없음
// rs.getString("member_id") : 현재 커서가 가리키고 있는 위치의 member_id 데이터를 String 으로 반환
// rs.getInt("money") : 현재 커서가 가리키고 있는 위치의 money 데이터를 Int 로 반환