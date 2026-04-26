package whatsinmypack.mvp_refactor.application.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import whatsinmypack.mvp_refactor.domain.user.domain.User;
import whatsinmypack.mvp_refactor.domain.user.port.UserPersistencePort;

/**
 * User 서비스.
 *
 * <p>
 * - 비즈니스 흐름 담당
 * - Persistence는 Port를 통해서만 접근
 * </p>
 * UseCase 분리는 좋은 수단이지만, 도메인 수정 방어 목표의 관점에서 현재는 과할 수 있다 판단
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserPersistencePort userPersistencePort;

    /**
     * 이메일 기반 단건 조회
     */
    public User getUser(String email) {
        return userPersistencePort.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 이메일"));
    }

    /**
     * 생성 (간단 예시)
     */
    public User createUser(String email, String nickname) {
        User user = User.builder()
                .kakaoId(1L) // 카카오 로그인을 담당하는 OAuth 반영하면 그때 맞춰 수정
                .email(email)
                .nickname(nickname)
                .build();

        return userPersistencePort.save(user);
    }
}
