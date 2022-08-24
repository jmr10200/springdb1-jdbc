package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV4_1;
import hello.jdbc.repository.MemberRepositoryV4_2;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 예외 누수 문제 해결
 * SQLException 제거
 * <p>
 * MemberRepository interface 의존
 * MemberRepositoryV4_2 이용
 */
@Slf4j
@SpringBootTest
class MemberServiceV4Test2 {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberServiceV4 memberService;

    @AfterEach
    void afterEach() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @TestConfiguration
    static class TestConfig {

        private final DataSource dataSource;

        // 생성자를 이용하여 스프링 부트가 만들어준 데이터소스 빈을 주입 받을 수도 있다.
        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        MemberRepository memberRepository() {
            // MemberRepositoryV4_2 이용
            return new MemberRepositoryV4_2(dataSource);
        }

        @Bean
        MemberServiceV4 memberServiceV4() {
            return new MemberServiceV4(memberRepository());
        }
    }

    /**
     * AOP 프록시 적용 확인
     */
    @Test
    void AopCheck() {
        // memberService 에 EnhancerBySpringCGLIB... 부분으로 프록시(CGLIB) 적용 확인 가능
        log.info("memberService class={}", memberService.getClass());
        // memberRepository 는 적용하지 않았으므로 프록시가 적용되지 않는다.
        log.info("memberRepository class={}", memberRepository.getClass());
        assertThat(AopUtils.isAopProxy(memberService)).isTrue();
        assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
    }

    @Test
    @DisplayName("정상 : 이체 성공")
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

// 정리
// 예외에 대한 부분 정리했다.
// 스프링이 예외를 추상화해준 덕분에, 서비스 계층은 특정 리포지토리의 구현 기술과 예외에 종속적이지 않게 되었다.
// 따라서 서비스 계층은 특정 구현 기술이 변경되어도 그대로 유지할 수 있게 되었다.
// 즉, DI를 다시 제대로 활용할 수 있게 되었다.
// 추가로 서비스 계층에서 예외를 잡아서 복구해야하는 경우, 예외가 스프링이 제공하는 데이터 접근 예외로 변경되어서
// 서비스 계층에 넘어오기 때문에 필요한 경우 예외를 잡아 복구하면 된다.