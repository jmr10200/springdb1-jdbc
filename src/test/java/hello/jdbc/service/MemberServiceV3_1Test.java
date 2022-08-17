package hello.jdbc.service;

import hello.jdbc.connection.ConnectionConstant;
import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 트랜젝션 - 트랜젝션 매니저
 */
class MemberServiceV3_1Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    private MemberRepositoryV3 memberRepository;
    private MemberServiceV3_1 memberService;

    @BeforeEach
    void setUp() {
        // DataSource -> JDBC 기술 사용
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        // new DataSourceTransactionManager(dataSource) : JDBC 용 트랜젝션 매니저
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);

        memberRepository = new MemberRepositoryV3(dataSource);
        memberService = new MemberServiceV3_1(transactionManager, memberRepository);
    }

    @AfterEach
    void afterEach() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("정상 : 이체성공")
    void accountTransfer() throws SQLException {
        // given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        // when
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        // then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("예외 : 이체실패")
    void accountTransferException() throws SQLException {
        // given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        // when
        assertThatThrownBy(() ->
                memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        // then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberEx = memberRepository.findById(memberEx.getMemberId());
        // rollback 확인
        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberEx.getMoney()).isEqualTo(10000);
    }
}
/* 트랜젝션 매니저2 */
// 흐름 이해
// 클라이언트 요청으로 서비스 로직 실행
// 서비스 계층에서 트랜젝션 시작 : transactionManger.getTransaction()
// 트랜젝션 매니저는 datasource 사용하여 connection 생성
// connection 을 수동 커밋으로 설정후 트랜젝션 시작
// connection 을 트랜젝션 동기화 매니저에 저장
// 트랜젝션 동기화 매니저는 ThreadLocal 에 보관 (멀티 쓰레드 환경에 안전하게 커넥션 보관)
// 서비스가 비즈니스 로직 실행하면, 리포지토리가 실행 (커넥션 파라미터 전달 x)
// 리포지토리는 DataSourceUtils.getConnection() 으로 트랜젝션 동기화 매니저에서 커넥션 꺼내 사용
// 비즈니스 로직 종료시 트랜젝션 동기화 매니저 통해 동기화된 커넥션을 획득하여 커밋 또는 롤백 실행
// 전체 리소스 정리
//   트랜젝션 동기화 매니저 정리 (ThreadLocal 은 사용 후 꼭 정리해야 함)
//   conn.setAutoCommit(true) 오토커밋 되돌리기, (커넥션 풀 고려하기 위함)
//   conn.close() 커넥션 종료 (커넥션 풀이면 커넥션 풀에 반환된다)

// 정리
// 트랜젝션 추상화 덕분에 JDBC 코드에 의존하지 않는다.
// 이후 DB 변경되면 DataSourceTransactionManager 에서 JpaTransactionManager 등으로 변경
// java.sql.SQLException 은 리포지토리에서 런타임으로 던져줘야 한다.
// 트랜젝션 동기화 매니저 덕분에 Connection 을 파라미터로 넘기지 않아도 된다.
