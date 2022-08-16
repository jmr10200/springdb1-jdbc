package hello.jdbc.service;

import hello.jdbc.connection.ConnectionConstant;
import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 기본동작, 트랜젝션이 없으면 문제가 발생한다.
 */
class MemberServiceV1Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberEx";
    public static final String MEMBER_EX = "ex";

    private MemberRepositoryV1 memberRepository;
    private MemberServiceV1 memberService;

    @BeforeEach
    void setUp() {
        // 각 테스트 실행 전
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        memberRepository = new MemberRepositoryV1(dataSource);
        memberService = new MemberServiceV1(memberRepository);

    }

    @AfterEach
    void after() throws SQLException {
        // 다음 테스트에 영향주지 않기위해, 각 테스트 종료후 데이터 삭제
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("정상 케이스 : 이체성공")
    void accountTransfer() throws SQLException {
        // given : 테스트할 데이터 준비
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        // when : 테스트할 로직 실행
        memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000);

        // then : 테스트 결과 검증
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberEx.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);

    }

    @Test
    @DisplayName("에러 케이스 : 이체실패")
    void accountTransferEx() throws SQLException {
        // given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        // when
        memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000);

        // then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberEx.getMemberId());
        // memberA 만 변경되고 memberEx 는 예외발생으로 변경되지 않는다.
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(10000);
    }
}