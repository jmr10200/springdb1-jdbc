package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.connection.ConnectionConstant;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.ConnectionConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryV1Test {

    MemberRepositoryV1 repository;

    // SetUp
    @BeforeEach
    void beforeEach() {
        // 기본 DriverManager - 항상 새로운 커넥션 획득
//         DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        // DriverManagerDataSource 를 사용하는 경우 : conn0~5 로 항상 커넥션이 생성되어 사용된다.
        // get connection=conn0: url=jdbc:h2:tcp://localhost/~/test
        // get connection=conn1: url=jdbc:h2:tcp://localhost/~/test

        // 커넥션 풀링 : HikariProxyConnection -> JdbcConnection
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        // 커넥션 풀링 : 아래와 같이 모두 conn0 를 이용한다. 하나가 재사용된다.
        // get connection=HikariProxyConnection@405036720 wrapping conn0: url=jdbc:h2:tcp://localhost/~/test ...생략
        // get connection=HikariProxyConnection@2088582214 wrapping conn0: url=jdbc:h2:tcp://localhost/~/test ...생략

        // DataSource 주입하여 RepositoryV1 생성
        repository = new MemberRepositoryV1(dataSource);
    }

    @Test
    void crud() throws SQLException {
        log.info("crud() start");

        // save
        Member member = new Member("memberV1", 10000);
        repository.save(member);

        // findById
        Member memberById = repository.findById(member.getMemberId());
        assertThat(memberById).isNotNull();
        assertThat(memberById).isEqualTo(member);

        // update : 10000 -> 20000
        repository.update(member.getMemberId(), 20000);
        Member updatedMember = repository.findById(member.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20000);

        // delete
        repository.delete(member.getMemberId());
        assertThatThrownBy(() -> repository.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class);

    }
}
/* DI + OCP : DataSource 사용에의한 장점 */
// DriverManagerDataSource -> HikariDataSource 로 변경해도 MemberRepositoryV1 의 코드는 변경할 필요가 없다.
// MemberRepositoryV1 는 DataSource 인터페이스에만 의존하기 때문이다.
