package whatsinmypack.mvp_refactor.adapter.out.persistence.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import whatsinmypack.mvp_refactor.domain.user.domain.User;
import whatsinmypack.mvp_refactor.domain.user.port.UserPersistencePort;

/**
 * 갈아끼우기용 임시 인메모리 어댑터
 */
@Component
@Profile("temp")
public class UserInMemoryAdapter implements UserPersistencePort {

    private final Map<Long, User> store = new HashMap<>();
    private final Map<String, User> emailStore = new HashMap<>();

    @Override
    public User save(User user) {
        store.put(user.getId(), user);
        emailStore.put(user.getEmail(), user);
        return user;
    }

    @Override
    public void delete(Long id) {
        store.remove(id);
    }

    @Override
    public Optional<User> findByIdOptional(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(emailStore.get(email));
    }

    // 매퍼는 생략, 어차피 도메인 객체 자체를 저장하고 있으니
}
