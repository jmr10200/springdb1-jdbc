package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * 트랜젝션 - 트랜젝션 매니저
 * DataSourceUtils.getConnection()
 * DataSourceUtils.releaseConnection()
 * Connection 을 파라미터로 전달하는 부분이 삭제된다.
 */
@Slf4j
public class MemberRepositoryV3 {

    private final DataSource dataSource;

    public MemberRepositoryV3(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 등록
     */
    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values(?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            log.error("save error", e);
            throw e;
        } finally {
            close(conn, pstmt, null);
        }
    }

    /**
     * 조회
     */
    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId = " + memberId);
            }
        } catch (SQLException e) {
            log.error("find error", e);
            throw e;
        } finally {
            close(conn, pstmt, rs);
        }

    }

    /**
     * 업데이트
     */
    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money = ? where member_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("update error", e);
            throw e;
        } finally {
            close(conn, pstmt, null);
        }
    }

    /**
     * 삭제
     */
    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("delete error", e);
            throw e;
        } finally {
            close(conn, pstmt, null);
        }
    }

    /**
     * 리소스 정리
     */
    private void close(Connection conn, Statement stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        // 주의! 트랜젝션을 사용하려면 DataSourceUtils 를 사용해야 한다.
        // close() 을 실행하면 직접 닫아버려서 커넥션이 유지되지 않는다.
        DataSourceUtils.releaseConnection(conn, dataSource);
        // DataSourceUtils.releaseConnection() 의 동작
        // 트랜젝션을 사용하기 위해 동기화된 커넥션은 커넥션을 닫지 않고 그대로 유지해준다.
        // 트랜젝션 동기화 매니저가 관리하는 커넥션이 없는 경우, 해당 커넥션을 close 한다.
    }

    /**
     * Connection 획득
     */
    private Connection getConnection() {
        // 주의! 트랜젝션 동기화를 사용하려면 DataSourceUtils 를 사용해야 함
        Connection conn = DataSourceUtils.getConnection(dataSource);
        // DataSourceUtils.getConnection() 의 동작
        // 트랜젝션 동기화 매니저가 관리하는 커넥션이 있으면 해당 커넥션을 반환한다.
        // 트랜젝션 동기화 매니저가 관리하는 커넥션이 없는 경우, 새로운 커넥션을 생성해서 반환한다.
        log.info("get connection={} class={}", conn, conn.getClass());
        return conn;
    }
}
