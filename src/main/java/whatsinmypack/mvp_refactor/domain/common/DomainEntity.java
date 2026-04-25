package whatsinmypack.mvp_refactor.domain.common;

import java.time.LocalDateTime;

/**
 * 서버 레벨에서 모든 도메인 객체가 반드시 이행해야 하는 기술적 행위 계약.
 * <p>
 * 도메인의 비즈니스 행위가 아닌,
 * 인프라 레이어(JPA)와의 연결을 위한 최소한의 계약만 정의한다.
 * <p>
 * 도메인 상태(필드)가 아무리 바뀌어도 이 계약은 유지되어야 한다.
 * 새 도메인이 추가될 때 이 인터페이스를 구현하는 것만으로
 * 서버 레벨 기술 계약이 컴파일 타임에 강제된다.
 * <p>
 * [사용 예시]<p>
 * class Pack implements DomainEntity<PackJpaEntity> { ... }<p>
 * class User implements DomainEntity<UserJpaEntity> { ... }
 *
 * @param <E> 대응하는 JPA 엔티티 타입
 * @param <ID> 대응하는 JPA 엔티티의 식별자 타입
 */
public interface DomainEntity<E, ID> {

    /**
     * 도메인 객체 → JPA 엔티티 변환 계약.
     * <p>
     * 어댑터는 이 메서드만 믿고 save()를 구현한다.<p>
     * 컬럼이 추가/삭제되면 구현체 내부만 수정하면 되고,
     * 어댑터 코드는 건드리지 않아도 된다.
     */
    E toJpaEntity();

    /**
     * 식별자 반환.<p>
     * 단순 PK(Long)뿐 아니라 복합키 VO도 수용할 수 있도록 타입을 ID로 열어둔다. 구현체에서 실제 타입으로 오버라이드해서 사용.
     */
    ID getId();

    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}
