package whatsinmypack.mvp_refactor.adapter.out.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import whatsinmypack.mvp_refactor.global.BaseJpaEntity;

/**
 * User JPA 엔티티.
 *
 * <p>
 * - DB 테이블(users)에 직접 매핑되는 객체
 * - 도메인 로직을 포함하지 않는다
 * </p>
 *
 * <p>
 * ※ 이 객체는 "저장 구조"만 표현하며,
 * 비즈니스 규칙은 도메인 객체에서 처리한다.
 * </p>
 */
@Entity
@Table(name = "users_refactor")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long kakaoId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;
}
