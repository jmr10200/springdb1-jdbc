package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {

        // save
        Member member = new Member("memberV0", 100100);
        repository.save(member);

        // findById
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember={}", findMember);
        // 주의! Member 에는 Lombok 의 @Data 가 설정되어있다.
        // @Data 는 equals() 도 오버라이딩하고 있기 때문에 가능하다.
        assertThat(findMember).isEqualTo(member);

        // update : money 변경
        repository.update(member.getMemberId(), 21200);
        Member updatedMember = repository.findById(member.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(21200);

        // delete
        repository.delete(member.getMemberId());
        // 삭제 정보를 조회하면 없기 때문에 예외 발생할 것
        // findById() 에 예외처리 해두었던 NoSuchElementException 이 발생한다
        assertThatThrownBy(() -> repository.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class);

    }

}