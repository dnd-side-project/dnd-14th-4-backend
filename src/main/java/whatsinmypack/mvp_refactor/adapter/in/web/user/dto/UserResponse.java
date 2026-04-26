package whatsinmypack.mvp_refactor.adapter.in.web.user.dto;

import java.time.LocalDateTime;
import whatsinmypack.mvp_refactor.domain.user.domain.User;

/**
 * 유저 응답 DTO.
 */
public record UserResponse(
        Long id,
        Long kakaoId,
        String email,
        String nickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getKakaoId(),
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
