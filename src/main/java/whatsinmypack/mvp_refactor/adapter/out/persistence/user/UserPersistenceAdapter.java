package whatsinmypack.mvp_refactor.adapter.out.persistence.user;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import whatsinmypack.mvp_refactor.domain.user.domain.User;
import whatsinmypack.mvp_refactor.domain.user.port.UserPersistencePort;

/**
 * User Persistence Adapter.
 *
 * <p>
 * - UserPersistencePort 구현체
 * - JPA를 사용한 실제 DB 접근 담당
 * </p>
 *
 * <p>
 * 역할:
 * - Repository 호출
 * - Entity ↔ Domain 변환
 * </p>
 */
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {

    private final UserJpaRepository repository;

    @Override
    public Optional<User> findByIdOptional(Long id) {
        return repository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        UserJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(this::toDomain);
    }

    // ===== mapping ===== → 매퍼 구현으로 별도 분리할지는 선택의 문제

    /**
     * JPA Entity → Domain 변환.
     */
    private User toDomain(UserJpaEntity entity) {
        return User.builder()
                .id(entity.getId())
                .kakaoId(entity.getKakaoId())
                .email(entity.getEmail())
                .nickname(entity.getNickname())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Domain → JPA Entity 변환.
     */
    private UserJpaEntity toEntity(User user) {
        return UserJpaEntity.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}
