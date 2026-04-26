package whatsinmypack.mvp_refactor.adapter.in.web.user.dto;

/**
 * 유저 생성 요청 DTO.
 */
public record CreateUserRequest(
        String email,
        String nickname
) {
}
