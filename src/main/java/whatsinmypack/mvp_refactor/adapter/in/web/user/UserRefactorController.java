package whatsinmypack.mvp_refactor.adapter.in.web.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import whatsinmypack.mvp_refactor.application.user.UserRefactorService;
import whatsinmypack.mvp_refactor.domain.user.domain.User;

/**
 * User API Controller.
 *
 * <p>
 * - HTTP 요청 진입점
 * - Service 호출만 담당
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserRefactorController {

    private final UserRefactorService userRefactorService;

    @GetMapping("/{email}")
    public User getUser(@PathVariable String email) {
        return userRefactorService.getUser(email);
    }

    @PostMapping
    public User createUser(
            @RequestParam String email,
            @RequestParam String nickname
    ) {
        return userRefactorService.createUser(email, nickname);
    }
}
