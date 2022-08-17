package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.SQLException;

/**
 * 트랜젝션 - 트랜젝션 매니저
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

    // 트랜젝션 매니저 주입
    // JDBC 기술을 사용하고 있기 때문에, DataSourceTansactionManager 구현체를 주입받아야한다.
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) {

        // 트랜젝션 start
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        // 현재 트랜젝션의 상태정보가 포함된다.
        // new DefaultTransactionDefinition() 는 트랜젝션과 관련된 옵션을 지정할 수 있다.

        try {
            // 비즈니스로직
            bizLogic(fromId, toId, money);
            // 성공시 commit
            transactionManager.commit(status);
        } catch (Exception e) {
            // 실패시 rollback
            transactionManager.rollback(status);
            throw new IllegalStateException(e);
        }
    }

    /**
     * 비즈니스 로직
     */
    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validate(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }

    private void validate(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
