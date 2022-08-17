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
    // JDBC 기술을 사용하고 있기 때문에, DataSourceTransactionManager 구현체를 주입받아야한다.
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
/* 가장 단순하면서 많이 사용하는 어플리케이션 구조 */
// @Controller : 프레젠테이션 계층 / UI 관련 처리, 웹 요청 응답, 클라이언트 요청 검증, 서블릿과 같은 HTTP 웹 기술, 스프링 MVC
// @Service : 서비스 계층 / 비즈니스 로직 담당 , 가급적 특정 기술에 의존하지 않고 순수 자바 코드로 작성
// @Repository : 데이터 접근 계층 / 실제 DB 접근 코드, JDBC, JPA, Redis, Mongo 등..

// 여기서 가장 중요한 것은 핵심 비즈니스 로직이 들어있는 '서비스 계층'이다
// 시간이 흘러 UI, DB 등이 변경된다해도, 비즈니스로직은 최대한 변경없이 유지되어야 한다.
// 그렇게 하기위해, 특정 기술에 의존적이지 않게 순수 자바코드로 개발하는 것이 좋다.

// MemberServiceV1 : 특정 기술에 의존하지 않고 순수한 비즈니스 로직만 존재하지만, throws SQLException 이 정의된다.
// 이 부분은 memberRepository 에서 해결해야한다.
// MemberServiceV2 : 트랜젝션은 비즈니스 로직이 있는 서비스 계층에서 시작하는 것이 좋다.
// 그런데 문제는 트랜젝션은 javax.sql.DataSource, java.sql.Connection, java.sql.SQLException 같은 JDBC 기술에 의존해야한다.
// 결과적으로 비즈니스로직보다 JDBC 트랜젝션 처리 때문에 복잡해지며, 향후 DB 변경이 있을때 코드 변경이 수반된다.
// 즉, 유지보수가 어렵다!

/* 문제점 정리 */
// 트랜젝션 문제 : JDBC 구현 기술이 서비스 계층에 누수, 트랜젝션 코드 반복, 트랜젝션 동기화문제
// 예외 누수 문제 : SQLException 이 서비스 계층에 누수된다.
// JDBC 반복 문제 : try, catch, finally 코드 반복이 많다.

// 스프링이 제공하는 트랜젝션매니저는 크게 2가지 역할을 한다.
// 트랜젝션 추상화, 리소스 동기화

/* 1. 트랜젝션 추상화 */
// 트랜젝션은 테이터 접근기술의 구현마다 사용하는 방법이 다르다.
//   JDBC : conn.setAutoCommit(false)
//   JPA : transaction.begin()
// 이렇게 사용하면 DB 변경시 트랜젝션 코드 변경이 발생한다. 이를 해결하기 위한게 트랜젝션 추상화이다.
// 가장 단순하게 생각하면 interface 를 생성하고 각 기술에맞는 구현체를 만드는 것이다.
// 이렇게 하면 DI 덕분에 OCP 를 지키게 된다.

// 스프링은 이를 제공해준다 : PlatformTransactionManager 인터페이스
// org.springframework.transaction.PlatformTransactionManager
// getTransaction() : 트랜젝션 시작
// commit() : 커밋
// rollback() : 롤백

// 참고 : 스프링 5.3 부터 JDBC 트랜젝션 관리시 DataSourceTransactionManger 를 상속받아
// 기능확장한 JdbcTransactionManager 를 제공한다. 둘의 기능 차이는 크지 않으므로 같다고 이해하면 된다.

/* 2. 트랜젝션 동기화 */
// 트랜젝션을 유지하려면 시작부터 끝까지 리소스가 유지되어야 한다. (Connection 유지)
// 커넥션을 동기화(유지)하기위해 파라미터로 전달하면 소스가 지저분하며, 여러 문제가 발생할 수 있다.
// 스프링은 트랜젝션 동기화 매니저를 제공한다. ThreadLocal 를 사용해서 커넥션을 동기화 해준다.

// 참고 : ThreadLocal 을 사용하면, 각각 쓰레드마다 별도의 저장소가 부여되므로 해당 쓰레드만 해당 데이터에 접근가능하다.

// 동작방식
// 트랜젝션 매니저는 datasource 통해 커넥션 만들고 트랜젝션 시작
// 트랜젝션 매니저는 커넥션을 트랜젝션 동기화 매니저에 보관
// 리포지토리는 트랜젝션 동기화 매니저에서 커넥션을 꺼내서 사용 (파라미터 사용x)
// 트랜젝션 종료되면 트랜젝션 매니저는 트랜젝션 동기화 매니저에 보관된 커넥션으로 트랜젝션 종료하고 커넥션 close

// 트랜젝션 동기화 매니저 : TransactionSynchronizationManager
// org.springframework.transaction.support.TransactionSynchronizationManager
