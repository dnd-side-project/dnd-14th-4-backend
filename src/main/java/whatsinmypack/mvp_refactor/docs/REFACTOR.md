# 리팩토링 코드 스케치 (mvp_refactor)

---

## 1. 패키지 트리 구조

```
mvp_refactor
├── adapter
│   ├── in
│   │   └── web
│   │       ├── item
│   │       │   ├── ItemController.java
│   │       │   ├── req
│   │       │   │   ├── CreateItemRequest.java
│   │       │   │   └── UpdateItemRequest.java
│   │       │   └── res
│   │       │       └── ItemResponse.java
│   │       ├── pack
│   │       │   ├── PackController.java
│   │       │   ├── req
│   │       │   │   ├── CreatePackRequest.java
│   │       │   │   └── UpdatePackRequest.java
│   │       │   └── res
│   │       │       └── PackResponse.java
│   │       └── user
│   │           ├── UserController.java
│   │           └── res
│   │               └── UserResponse.java
│   └── out
│       ├── persistence
│       │   ├── item
│       │   │   ├── ItemJpaEntity.java          ← @Entity, 테이블에만 종속
│       │   │   ├── ItemJpaRepository.java
│       │   │   ├── ItemMapper.java             ← JpaEntity ↔ 도메인 객체 변환 전담
│       │   │   └── ItemPersistenceAdapter.java ← CrudPort 구현체
│       │   ├── pack
│       │   │   ├── PackJpaEntity.java
│       │   │   ├── PackJpaRepository.java
│       │   │   ├── PackMapper.java
│       │   │   └── PackPersistenceAdapter.java
│       │   └── user
│       │       ├── UserJpaEntity.java
│       │       ├── UserJpaRepository.java
│       │       ├── UserMapper.java
│       │       └── UserPersistenceAdapter.java
│       └── query
│           ├── PackQueryAdapter.java           ← JdbcTemplate 기반 복합 조회 구현체
│           └── ItemQueryAdapter.java
├── application
│   ├── item
│   │   ├── create
│   │   │   ├── CreateItemCommand.java
│   │   │   ├── CreateItemService.java
│   │   │   └── CreateItemUseCase.java
│   │   ├── delete
│   │   │   ├── DeleteItemService.java
│   │   │   └── DeleteItemUseCase.java
│   │   └── update
│   │       ├── UpdateItemCommand.java
│   │       ├── UpdateItemService.java
│   │       └── UpdateItemUseCase.java
│   ├── pack
│   │   ├── create
│   │   │   ├── CreatePackCommand.java
│   │   │   ├── CreatePackService.java
│   │   │   └── CreatePackUseCase.java
│   │   ├── delete
│   │   │   ├── DeletePackService.java
│   │   │   └── DeletePackUseCase.java
│   │   └── update
│   │       ├── UpdatePackCommand.java
│   │       ├── UpdatePackService.java
│   │       └── UpdatePackUseCase.java
│   └── user
│       └── profile
│           ├── UpdateProfileCommand.java
│           ├── UpdateProfileService.java
│           └── UpdateProfileUseCase.java
├── domain
│   ├── common
│   │   ├── DomainEntity.java                  ← 기술적 행위 계약 인터페이스
│   │   └── CrudPort.java                      ← 제네릭 저장소 추상화
│   ├── item
│   │   ├── Item.java                          ← 순수 도메인 객체
│   │   └── port
│   │       ├── ItemPersistencePort.java        ← CrudPort<Item, Long> 확장
│   │       └── ItemQueryPort.java             ← 복합 조회 전용 포트
│   ├── pack
│   │   ├── Pack.java
│   │   └── port
│   │       ├── PackPersistencePort.java
│   │       └── PackQueryPort.java
│   └── user
│       ├── User.java
│       └── port
│           └── UserPersistencePort.java
└── global
    ├── config
    │   ├── JdbcConfig.java
    │   ├── SecurityConfig.java
    │   └── SwaggerConfig.java
    ├── entity
    │   └── BaseJpaEntity.java                 ← createdAt, updatedAt 공통 필드
    └── security
        └── ...
```

---

## 2. 레이어별 코드 스케치

### 2-1. domain/common — 공통 계약

```java
// DomainEntity.java
// 어떤 도메인이든 서버 레벨에서 반드시 필요한 기술적 행위를 인터페이스로 고정
public interface DomainEntity<E> {
    E toJpaEntity();
    Long getId();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}
```

```java
// CrudPort.java
// 어떤 도메인이든 수용 가능한 공통 저장소 추상화
// 도메인이 추가/삭제되어도 이 인터페이스 자체는 건드리지 않음
public interface CrudPort<T, ID> {
    T findById(ID id);
    Optional<T> findByIdOptional(ID id);
    T save(T domain);
    void delete(ID id);
    T findByIdWithLock(ID id);  // 비관적 락 조회
}
```

---

### 2-2. domain/pack — 도메인 객체 + 포트

```java
// Pack.java
// 순수 도메인 객체. @Entity 없음. 비즈니스 상태와 DomainEntity 계약만 보유.
public class Pack implements DomainEntity<PackJpaEntity> {

    private Long id;
    private Long ownerId;
    // 기획이 바뀌면 필드가 추가/삭제될 수 있음
    // 하지만 아래 계약은 항상 유지됨

    @Override
    public Long getId() { return id; }

    @Override
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // 변환 계약 — 어댑터가 이 메서드만 믿고 구현
    @Override
    public PackJpaEntity toJpaEntity() {
        return PackJpaEntity.builder()
            .id(this.id)
            .ownerId(this.ownerId)
            // 필드가 늘거나 줄어도 여기만 수정
            .build();
    }
}
```

```java
// PackPersistencePort.java
// CrudPort를 확장. Pack 도메인 전용 CRUD 계약.
// 도메인 특화 메서드가 필요하면 여기에 추가.
public interface PackPersistencePort extends CrudPort<Pack, Long> {
    List<Pack> findAllByOwnerId(Long ownerId);
}
```

```java
// PackQueryPort.java
// 도메인 경계를 넘는 복합 조회 전용 포트.
// JPA가 아닌 JdbcTemplate 기반 구현체와 연결됨.
// 실제 조인 케이스가 확정될 때 메서드를 추가하면 됨.
public interface PackQueryPort {
    List<PackWithItemCountDto> findPacksWithItemCount(Long userId);
    // 조인 케이스가 생기면 여기에 추가
}
```

```java
// PackWithItemCountDto.java
// 복합 조회 전용 DTO. 도메인 객체가 아님.
// JPA 엔티티를 거치지 않고 JdbcTemplate 결과를 직접 매핑.
public record PackWithItemCountDto(
    Long packId,
    String packName,
    int itemCount
) {}
```

---

### 2-3. adapter/out/persistence/pack — JPA 어댑터

```java
// PackJpaEntity.java
// 테이블에만 종속. 도메인 로직 없음.
@Entity
@Table(name = "pack")
@NoArgsConstructor
public class PackJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ownerId;
    // 테이블 컬럼만 반영. 컬럼이 바뀌면 여기만 수정.
}
```

```java
// PackMapper.java
// JpaEntity ↔ 도메인 객체 변환 전담.
// 변환 로직이 어댑터 밖으로 새어나가지 않음.
public class PackMapper {

    public static Pack toDomain(PackJpaEntity entity) {
        return Pack.builder()
            .id(entity.getId())
            .ownerId(entity.getOwnerId())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    // toJpaEntity는 Pack 도메인 객체의 toJpaEntity()가 담당
    // 매퍼는 toDomain 방향만 책임짐
}
```

```java
// PackPersistenceAdapter.java
// CrudPort 구현체. JpaEntity와 도메인 객체 사이의 변환을 어댑터가 독점.
@RequiredArgsConstructor
@Component
public class PackPersistenceAdapter implements PackPersistencePort {

    private final PackJpaRepository repository;

    @Override
    public Pack findById(Long id) {
        return repository.findById(id)
            .map(PackMapper::toDomain)
            .orElseThrow(() -> new EntityNotFoundException("Pack not found: " + id));
    }

    @Override
    public Pack save(Pack pack) {
        PackJpaEntity saved = repository.save(pack.toJpaEntity());
        return PackMapper.toDomain(saved);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Pack findByIdWithLock(Long id) {
        return repository.findByIdWithLock(id)
            .map(PackMapper::toDomain)
            .orElseThrow(() -> new EntityNotFoundException("Pack not found: " + id));
    }

    @Override
    public List<Pack> findAllByOwnerId(Long ownerId) {
        return repository.findAllByOwnerId(ownerId).stream()
            .map(PackMapper::toDomain)
            .toList();
    }
}
```

```java
// PackJpaRepository.java
public interface PackJpaRepository extends JpaRepository<PackJpaEntity, Long> {
    List<PackJpaEntity> findAllByOwnerId(Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PackJpaEntity p WHERE p.id = :id")
    Optional<PackJpaEntity> findByIdWithLock(@Param("id") Long id);
}
```

---

### 2-4. adapter/out/query — JdbcTemplate 복합 조회

```java
// PackQueryAdapter.java
// 도메인 경계를 넘는 복합 조회는 JPA 대신 JdbcTemplate 사용.
// N+1 없이 필요한 조인을 직접 SQL로 제어.
@RequiredArgsConstructor
@Component
public class PackQueryAdapter implements PackQueryPort {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<PackWithItemCountDto> findPacksWithItemCount(Long userId) {
        String sql = """
            SELECT p.id       AS pack_id,
                   p.name     AS pack_name,
                   COUNT(pi.item_id) AS item_count
            FROM pack p
            LEFT JOIN pack_item pi ON p.id = pi.pack_id
            WHERE p.owner_id = ?
            GROUP BY p.id, p.name
            """;

        return jdbcTemplate.query(sql,
            (rs, rowNum) -> new PackWithItemCountDto(
                rs.getLong("pack_id"),
                rs.getString("pack_name"),
                rs.getInt("item_count")
            ),
            userId
        );
    }

    // 조인 케이스가 추가되면 메서드를 여기에 추가
    // PackQueryPort에 시그니처 추가 → 여기에 구현 추가
}
```

---

### 2-5. application/pack — 서비스

```java
// CreatePackService.java
// 서비스는 도메인 객체와 포트 인터페이스만 바라봄.
// JpaEntity, JdbcTemplate 등 인프라를 전혀 모름.
@RequiredArgsConstructor
@Service
public class CreatePackService implements CreatePackUseCase {

    private final PackPersistencePort packPersistencePort;

    @Override
    public Pack create(CreatePackCommand command) {
        Pack pack = Pack.builder()
            .ownerId(command.userId())
            // command에서 필드 매핑
            .build();

        return packPersistencePort.save(pack);
    }
}
```

```java
// GetPacksService.java
// 복합 조회가 필요한 케이스는 QueryPort를 통해 처리.
// 서비스는 QueryPort 인터페이스만 알고, JdbcTemplate은 모름.
@RequiredArgsConstructor
@Service
public class GetPacksService implements GetPacksUseCase {

    private final PackQueryPort packQueryPort;

    @Override
    public List<PackWithItemCountDto> getPacksWithItemCount(Long userId) {
        return packQueryPort.findPacksWithItemCount(userId);
    }
}
```

---

### 2-6. global/entity — 공통 JPA 베이스

```java
// BaseJpaEntity.java
// JPA 엔티티 공통 필드. 도메인 객체의 DomainEntity와는 별개.
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseJpaEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

---

## 3. 레이어 간 의존 방향 요약

```
[Controller]
     ↓
[UseCase 인터페이스]
     ↓
[Service]  ─────────────────────────────────────────┐
     │                                              │
     ↓ (단순 CRUD)              (복합 조회)            │
[PersistencePort]          [QueryPort]              │
 CrudPort<T,ID> 확장        JdbcTemplate 기반         │
     ↓                          ↓                   │
[PersistenceAdapter]       [QueryAdapter]           │
 JpaEntity ↔ 도메인 변환    SQL 직접 제어                │
     ↓                          ↓                   │
[JpaRepository]            [JdbcTemplate]           │
     ↓                          ↓                   │
                [PostgreSQL]                        │
                                                    │
[도메인 객체] ←────────────────────────────────────────┘
 DomainEntity<E> 구현
 toJpaEntity() 계약 보유
 순수 비즈니스 상태
```

### 변경이 발생했을 때 파급 범위

| 변경 원인 | 영향 범위 |
|---|---|
| 테이블 컬럼 추가/삭제 | `JpaEntity` + `Mapper` + `toJpaEntity()` |
| 도메인 통째로 추가 | 새 `DomainEntity` 구현체 + `CrudPort` 확장 + `Adapter` 추가 |
| 도메인 통째로 제거 | 해당 도메인 패키지 삭제, 나머지 무관 |
| 복합 조회 추가 | `QueryPort` 메서드 추가 + `QueryAdapter` 구현 추가 |
| 서비스 로직 변경 | `Service` 내부만, 포트/어댑터 무관 |