package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜젝션 - 파라미터 연동, 풀을 고려한 종료
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        Connection conn = dataSource.getConnection();

        try {
            // 트랜젝션 시작 : 오토커밋 off
            conn.setAutoCommit(false);

            // 비즈니스 로직 : 트랜젝션과 비즈니스 로직을 구분
            bizLogic(fromId, toId, money, conn);

            // 문제 없으면 commit
            conn.commit();
        } catch (Exception e) {
            // 에러나면 rollback
            conn.rollback();
            throw new IllegalStateException(e);
        } finally {
            // 리소스 정리
            release(conn);
        }
    }

    private void release(Connection conn) {
        if (conn != null) {
            try {
                // 커넥션 풀 고려하여 풀에 리턴하기전에 오토커밋으로 돌려놓기
                conn.setAutoCommit(true);
                conn.close();
            } catch (Exception e) {
                log.info("error", e);
            }
        }
    }

    private void bizLogic(String fromId, String toId, int money, Connection conn) throws SQLException {
        Member fromMember = memberRepository.findById(conn, fromId);
        Member toMember = memberRepository.findById(conn, toId);

        memberRepository.update(conn, fromId, fromMember.getMoney() - money);
        validate(toMember);
        memberRepository.update(conn, toId, toMember.getMoney() + money);
    }

    private void validate(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
