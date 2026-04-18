package whatsinmypack.mvp.adapter.in.web.pack;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import whatsinmypack.mvp.domain.contextCategory.entity.ContextCategory;
import whatsinmypack.mvp.domain.pack.entity.Pack;
import whatsinmypack.mvp.domain.user.entity.AuthProvider;
import whatsinmypack.mvp.domain.user.entity.User;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PackControllerTest {

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private MockMvc mockMvc;

    private Long packId;
    private User testUser;

    @BeforeEach
    void beforetest() {
        transactionTemplate.executeWithoutResult(status -> {
            testUser = User.builder()
                    .email("user_1@test.com")
                    .nickname("수건")
                    .authProvider(AuthProvider.KAKAO)
                    .profileImage("https://sugun.com/profile.png")
                    .build();
            entityManager.persist(testUser);

            ContextCategory contextCategory = ContextCategory.builder()
                    .name("회사")
                    .detail("출근 아이템")
                    .build();
            entityManager.persist(contextCategory);

            Pack pack = Pack.builder()
                    .title("출근 필수템")
                    .introduction("출근길에 유용한 아이템")
                    .user(testUser)
                    .contextCategory(contextCategory)
                    .build();
            entityManager.persist(pack);
            entityManager.flush();
            packId = pack.getId();
            entityManager.clear();
        });
    }

    @AfterEach
    void aftertest() {
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.createQuery("delete from Pack").executeUpdate();
            entityManager.createQuery("delete from ContextCategory").executeUpdate();
            entityManager.createQuery("delete from User").executeUpdate();
            entityManager.flush();
            entityManager.clear();
        });
    }

    @Test
    @DisplayName("조회행위 테스트 / 팩 상세 조회 API 컨트롤러 테스트")
    void getPack() throws Exception {
        assertNotNull(packId);
        mockMvc.perform(get("/api/v1/packs/{packId}", packId)
                        .header("Authorization", "Bearer user_1"))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(packId));
    }
}