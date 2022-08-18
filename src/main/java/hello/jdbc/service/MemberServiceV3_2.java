package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;

/**
 * 트랜젝션 - 트랜젝션 템플릿
 */
@Slf4j
public class MemberServiceV3_2 {

    private final TransactionTemplate transactionTemplate;
    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        // TransactionTemplate 는 TransactionManger 가 필요하므로 생성자에서 주입받아 생성
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;
    }

    /**
     * 이체
     */
    public void accountTransfer(String fromId, String toId, int money) {
        transactionTemplate.executeWithoutResult((status) -> {
            try {
                // 비즈니스 로직
                bizLogic(fromId, toId, money);
                // commit, rollback 코드가 제거됨
                // 트랜젝션 템플릿 기본 동작
                // 정상 동작 : 커밋
                // 런타임 예외 발생 : 롤백 (체크 예외면 커밋)
            } catch (SQLException e) {
                // 람다에서는 체크예외를 밖으로 던질 수 없기 때문에 런타임에러로 바꿔서 전달
                throw new IllegalStateException(e);
            }
        });
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

/* 트랜젝션 템플릿 */
// 트랜젝션 때문에 try, catch, finally 를 반복 작성하게 된다.
// 템플릿 콜백 패턴을 활용하여 반복문제를 해결할 수 있다.
// 스프링이 제공하는 TransactionTemplate 라는 클래스를 이용할 수 있다.
// execute() : 응답 값이 있을 때 사용
// executeWithoutResult() : 응답 값이 없을 때 사용

// 정리
// 트랜젝션 템플릿 덕분에, 트랜젝션 반복코드가 제거 되었다.
// 그럼에도 서비스 로직에 트랜젝션을 처리하는 기술이 함께 포함되어 있다.
// 서비스 계층에서 비즈니스 로직은 핵심 기능이고, 트랜젝션은 부가 기능인데,
// 이렇게 비즈니스 로직과 트랜젝션을 처리하는 기술 로직이 한 곳에 있으면 두 관심사를 하나의
// 클래스에서 처리하게 되므로 코드 유지보수가 어려워진다.
// 서비스 계층에서는 가능한한 핵심 비즈니스 로직만 있어야 한다.
// 트랜젝션 기술을 사용하면서도 의존하지 않게 해야한다.
