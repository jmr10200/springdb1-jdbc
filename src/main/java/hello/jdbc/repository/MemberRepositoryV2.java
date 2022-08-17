package hello.jdbc.repository;

import hello.jdbc.connection.ConnectionConstant;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.ConnectionConstant.*;

/**
 * JDBC - ConnectionParam
 */
@Slf4j
public class MemberRepositoryV2 {

    private final DataSource dataSource;

    public MemberRepositoryV2(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 등록
     */
    public Member save(Member member) throws SQLException {
        String sql = "insert into member (member_id, money) values(? , ?)";

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
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }
        } catch (SQLException e) {
            log.error("findById error", e);
            throw e;
        } finally {
            close(conn, pstmt, rs);
        }
    }

    /**
     * 조회 : Connection 유지
     */
    public Member findById(Connection conn, String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
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
            log.error("findById error", e);
            throw e;
        } finally {
            // 커넥션 유지 : connection 은 여기서 닫지 않는다.
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(pstmt);
        }
    }

    /**
     * 갱신
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
        } catch (SQLException e) {
            log.error("update error", e);
            throw e;
        } finally {
            close(conn, pstmt, null);
        }
    }

    /**
     * 갱신 : Connection 유지
     */
    public void update(Connection conn, String memberId, int money) throws SQLException {

        String sql = "update member set money = ? where member_id = ?";

        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("update error", e);
            throw e;
        } finally {
            // 커넥션 유지 : connection 은 여기서 닫지 않는다.
            JdbcUtils.closeStatement(pstmt);
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
    private void close(Connection conn, Statement stmt, ResultSet rs) throws SQLException {
        // 역순으로 close()
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(conn);
    }

    /**
     * Connection 취득
     */
    private Connection getConnection() throws SQLException {
        Connection conn = dataSource.getConnection();
        log.info("get connection={}, class={}", conn, conn.getClass());
        return conn;
    }
}
