package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

@RequiredArgsConstructor
public class MemberServiceV1 {

    private final MemberRepositoryV1 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        // fromMember 에서 toMember 에게 money 계좌이체 로직
        memberRepository.update(fromId, fromMember.getMoney() - money);
        validate(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);

    }

    private void validate(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이제중 예외 발생");
        }
    }
}
