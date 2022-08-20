package hello.jdbc.repository;

import hello.jdbc.domain.Member;

/**
 * 특정 기술에 종속되지 않는 interface
 * 구현체(impl)에서 런타임으로 변환시키자
 */
public interface MemberRepository {

    Member save(Member member);
    // 구현체(impl) 에서 런타임 예외로 변환시키지 않는다면 아래와 같이 특정 기술에 종속된다.
    // Member save(Member member) throws SQLException;
    // 이렇게 throws SQLException 체크예외가 선언되면 더이상 순수한 인터페이스가 아니다.
    // interface 의 목적은 구현체(impl) 를 쉽게 변경하기 위함이다.
    // 런타임 예외의 interface 는 선언할 필요가 없으니 특정 기술에 종속적이지 않다.

    Member findById(String memberId);

    void update(String memberId, int money);

    void delete(String memberId);

}
