package whatsinmypack.mvp_refactor.domain.user.port;

import java.util.Optional;
import whatsinmypack.mvp_refactor.domain.common.CrudPort;
import whatsinmypack.mvp_refactor.domain.user.domain.User;

/**
 * User 도메인 전용 Persistence Port.
 *
 * <p>
 * - 공통 CRUD는 CrudPort를 통해 제공
 * - 도메인 특화 조회는 여기서 확장
 * </p>
 */
public interface UserPersistencePort extends CrudPort<User, Long> {

    // email 기반으로 사용자 조회
    Optional<User> findByEmail(Long kakaoId);
}
