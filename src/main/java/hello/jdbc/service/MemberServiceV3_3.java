package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

/**
 * 트랜젝션 - @Transactional
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_3 {

    private final MemberRepositoryV3 memberRepository;

    // 스프링이 제공하는 트랜젝션 AOP 적용위해 @Transactional 을 사용함으로써 비즈니스로직만 남는다.
    // @Transactional 은 클래스에 붙여도 된다. 단, 이 경우 public 메소드가 대상이 된다.
    @Transactional
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        // 비즈니스 로직
        bizLogic(fromId, toId, money);
    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromMember.getMemberId(), fromMember.getMoney() - money);
        validate(toMember);
        memberRepository.update(toMember.getMemberId(), toMember.getMoney() + money);
    }

    private void validate(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
/* 트랜젝션 문제 해결 - 트랜젝션 AOP 이해 */
// 트랜젝션 추상화 (트랜젝션 편리하게 처리), 트랜젝션 템플릿 (반복적인 로직 해결) 을 해봐도 순수하게 비즈니스로직만 남길 수는 없었다.
// 이때, 스프링 AOP 를 통해 프록시를 도입하면 문제를 깔끔하게 해결할 수 있다.
// 프록시 도입 전 : 서비스 에서 트랜젝션을 직접 시작해야 한다.
// 프록시 도입 후 : 트랜젝션 프록시가 트랜젝션 처리 로직을 모두 가져가고 트랜젝션을 시작한 후에
// 실제 서비스를 대신 호출하므로 트랜젝션을 처리하는 객체와 비즈니스 로직의 서비스 계층을 분리할 수 있다.

// 스프링이 제공하는 트랜젝션 AOP
// 스프링이 제공하는 AOP 기능을 사용하면 프록시를 편리하게 적용할 수 있다.
// (@Aspect, @Advice, @Pointcut 를 사용해서 트랜젝션 처리)
// 스프링 부트를 사용하면 자동으로 등록해준다. 개발자는 @Transactional 을 붙여주면 된다.
// 스프링의 트랜젝션 AOP 는 이 어노테이션을 인식해서 트랜젝션 프록시를 적용해준다.
// @Transactional
// org.springframework.transaction.annotation.Transactional

// 참고 : 스프링 트랜젝션 AOP 처리를 위한 클래스 (스프링부트를 이용하면 빈으로 자동 등록됨)
// @Aspect 어드바이저: BeanFactoryTransactionAttributeSourceAdvisor
// @Pointcut 포인트컷: TransactionAttributeSourcePointcut
// @Advice 어드바이스: TransactionInterceptor