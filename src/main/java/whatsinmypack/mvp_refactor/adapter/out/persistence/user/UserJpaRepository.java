package whatsinmypack.mvp_refactor.adapter.out.persistence.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * User JPA Repository.
 *
 * <p>
 * - Spring Data JPA 인터페이스
 * <p>
 * - DB 접근 전담
 * </p>
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    // email 기반으로 조회
    Optional<UserJpaEntity> findByEmail(String email);
}
