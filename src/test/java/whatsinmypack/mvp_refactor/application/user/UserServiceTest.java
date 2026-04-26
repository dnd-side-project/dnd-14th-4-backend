package whatsinmypack.mvp_refactor.application.user;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import whatsinmypack.mvp_refactor.domain.user.domain.User;
import whatsinmypack.mvp_refactor.domain.user.port.UserPersistencePort;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL = "test@test.com";

    @Mock
    private UserPersistencePort userPersistencePort;

    @InjectMocks
    private UserService userService;

    @Test
    void 유저_조회_테스트() {
        User mockUser = User.builder()
                .id(1L)
                .email(EMAIL)
                .build();

        when(userPersistencePort.findByEmail(EMAIL)).thenReturn(Optional.ofNullable(mockUser));

        User result = userService.getUser(EMAIL);

        assertThat(result.getEmail()).isEqualTo("test@test.com");
    }
}