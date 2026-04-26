package whatsinmypack.mvp_refactor.domain.user;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import whatsinmypack.mvp_refactor.domain.common.DomainEntity;

/**
 * User 도메인 객체.
 * <p>
 * - 순수 비즈니스 상태를 표현하는 객체
 * - JPA, DB, 프레임워크에 대한 의존성 없음
 * <p>
 * ※ 이 객체는 "데이터 저장 방식"이 아닌
 * "비즈니스 상태"에만 집중한다.
 */
@Getter
@Builder
public class User implements DomainEntity<Long> {

    private Long id;
    private Long kakaoId;
    private String email;
    private String nickname;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 닉네임 변경.
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
