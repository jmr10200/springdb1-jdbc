package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV4_2;
import hello.jdbc.repository.MemberRepositoryV5;
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
 * JdbcTemplate 사용
 * <p>
 * MemberRepository interface 의존
 * MemberRepositoryV5 이용
 */
@Slf4j
@SpringBootTest
class MemberServiceV4Test3 {

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
            // MemberRepositoryV5 이용
            return new MemberRepositoryV5(dataSource);
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
// JdbcTemplate 는 JDBC 로 개발할 때 발생하는 반복을 대부분 해결해준다.
// 그 뿐만 아니라, 트랜젝션을 위한 커넥션 동기화는 물론이고, 예외 발생시 스프링 예외 변환기도 자동으로 실행해준다.

// 서비스 계층의 순수성
//  트랜젝션 추상화 + 트랜젝션 AOP 덕분에 서비스 계층의 순수성을 최대한 유지하면서 서비스 계층에서 트랜젝션을 사용할 수 있다.
//  스프링이 제공하는 예외 추상화와 예외 변환기 덕분에, 데이터 접근 기술이 변경되어도 서비스 계층의 순수성을 유지하면서 예외도 사용할 수 있다.
//  서비스 계층이 리포지토리 인터페이스에 의존한 덕분에 향후 리포지토리가 다른 구현기술로 변경되어도 서비스 계층을 순수하게 유지할 수 있다.
// 리포지토리에서 JDBC 를 사용하는 반복 코드가 JdbcTemplate 으로 대부분 제거되었다.