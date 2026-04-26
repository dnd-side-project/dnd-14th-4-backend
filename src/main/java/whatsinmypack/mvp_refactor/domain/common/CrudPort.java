package whatsinmypack.mvp_refactor.domain.common;

import java.util.Optional;

/**
 * 어떤 도메인이든 수용 가능한 공통 저장소 추상화.
 * <p>
 * 도메인이 추가/삭제되어도 이 인터페이스 자체는 건드리지 않는다.
 * 도메인 특화 메서드가 필요하면 이를 확장한 개별 Port에 추가 정의.
 * <p>
 * [사용 예시]
 * <p>
 * interface PackPersistencePort extends CrudPort<Pack, Long> { ... }
 * <p>
 * interface UserPersistencePort extends CrudPort<User, Long> { ... }
 *
 * @param <T>  도메인 객체 타입
 * @param <ID> 식별자 타입(복합키 수용도 가능하도록 별개의 타입)
 */
public interface CrudPort<T, ID> {

    /**
     * ID로 단건 조회. 없으면 Optional.empty().
     */
    Optional<T> findByIdOptional(ID id);

    /**
     * 저장 (신규 생성 및 수정 모두 커버).
     * 저장된 도메인 객체를 반환.
     */
    T save(T domain);

    /**
     * ID로 삭제.
     */
    void delete(ID id);

}
