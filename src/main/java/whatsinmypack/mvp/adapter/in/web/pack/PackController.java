package whatsinmypack.mvp.adapter.in.web.pack;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import whatsinmypack.mvp.adapter.in.web.pack.req.CreatePackRequest;
import whatsinmypack.mvp.adapter.in.web.pack.req.UpdatePackRequest;
import whatsinmypack.mvp.adapter.in.web.pack.res.PackDetailResponse;
import whatsinmypack.mvp.adapter.in.web.pack.res.PackRecommendationResponse;
import whatsinmypack.mvp.adapter.in.web.pack.res.PackSummaryResponse;
import whatsinmypack.mvp.adapter.in.web.pack.res.SlicePackResponse;
import whatsinmypack.mvp.adapter.in.web.pack.res.SliceResponse;
import whatsinmypack.mvp.application.pack.create.CreatePackUseCase;
import whatsinmypack.mvp.application.pack.delete.DeletePackUseCase;
import whatsinmypack.mvp.application.pack.getlist.GetPacksUseCase;
import whatsinmypack.mvp.application.pack.getlist.SearchPacksUseCase;
import whatsinmypack.mvp.application.pack.update.UpdatePackUseCase;
import whatsinmypack.mvp.application.wishlist.AddPackWishListUseCase;
import whatsinmypack.mvp.application.wishlist.RemovePackWishListUseCase;
import whatsinmypack.mvp.adapter.out.persistence.relation.ItemWishListJpaRepository;
import whatsinmypack.mvp.adapter.out.persistence.relation.PackWishListJpaRepository;
import whatsinmypack.mvp.domain.pack.entity.Pack;
import whatsinmypack.mvp.domain.relation.entity.PackItem;
import whatsinmypack.mvp.global.security.user.UserDetailsImpl;

@Tag(name = "Pack", description = "팩 관련 컨트롤러")
@RestController
@RequestMapping("/api/v1/packs")
@RequiredArgsConstructor
public class PackController {

    private final CreatePackUseCase createPackUseCase;
    private final SearchPacksUseCase searchPacksUseCase;
    private final GetPacksUseCase getPacksUseCase;
    private final UpdatePackUseCase updatePackUseCase;
    private final DeletePackUseCase deletePackUseCase;
    private final AddPackWishListUseCase addPackWishListUseCase;
    private final RemovePackWishListUseCase removePackWishListUseCase;
    private final PackWishListJpaRepository packWishListJpaRepository;
    private final ItemWishListJpaRepository itemWishListJpaRepository;

    @Operation(
            summary = "팩 생성",
            description = """
                    로그인한 유저가 기존에 등록한 아이템들을 선택하여 새로운 팩을 생성
                    - 아이템은 이미 존재(향후 예외처리 추가 예정)
                    - 컨텍스트 카테고리는 이름(name) 기준으로 조회
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "팩 생성 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PackDetailResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 필요",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = whatsinmypack.mvp.presentation.response.ApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "컨텍스트 카테고리 또는 아이템을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = whatsinmypack.mvp.presentation.response.ApiResponse.class)
                    )
            )
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public PackDetailResponse createPack(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(
                    description = "아이템 생성 요청 (JSON)",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreatePackRequest.class)))
            @Valid @RequestBody CreatePackRequest request
    ) {
        return PackDetailResponse.from(createPackUseCase.create(userDetails.getUser().getId(), request));
    }

    @Operation(
            summary = "팩 단건 조회",
            description = """
            특정 팩의 상세 정보를 조회
            
            조회 정보:
            - 팩 제목
            - 작성자 닉네임
            - 작성일
            - 팩 소개
            - 컨텍스트 카테고리
            - 팩에 포함된 아이템 목록
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "팩 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PackDetailResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "팩을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = whatsinmypack.mvp.presentation.response.ApiResponse.class
                            )
                    )
            )
    })
    @GetMapping("/{packId}")
    public PackDetailResponse getPack(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("packId") Long packId
    ) {
        Pack pack = getPacksUseCase.findById(packId);
        Long userId = userDetails.getUserId();

        boolean isPackInWishList = !packWishListJpaRepository
                .findWishlistedPackIdsByUserIdAndPackIds(userId, List.of(pack.getId()))
                .isEmpty();

        Set<Long> itemIds = pack.getPackItems().stream()
                .map(PackItem::getItem)
                .map(item -> item.getId())
                .collect(java.util.stream.Collectors.toSet());

        Set<Long> wishlistedItemIds = itemIds.isEmpty()
                ? Set.of()
                : new HashSet<>(itemWishListJpaRepository.findWishlistedItemIdsByUserIdAndItemIds(
                userId,
                List.copyOf(itemIds)
        ));

        return PackDetailResponse.from(pack, isPackInWishList, wishlistedItemIds);
    }

    @Operation(summary = "내 팩 전체 조회", description = "로그인한 유저의 작성 팩 목록을 최신순으로 조회")
    @GetMapping
    public List<PackSummaryResponse> getMyPackList(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return getPacksUseCase.findUserPacks(userDetails.getUser())
                .stream()
                .map(e -> PackSummaryResponse.from(e, userDetails.getUser().getNickname()))
                .toList();
    }


    @Operation(
            summary = "팩 검색",
            description = """
                키워드를 기반으로 팩을 검색
                
                검색 대상
                - 팩 제목
                - 팩 설명
                - 아이템 제목
                - 아이템 브랜드
                - 아이템 구매처
                
                컨텍스트 카테고리
                - contexts 파라미터가 없으면 전체 팩 대상 검색
                - 여러 개 전달 시 OR 조건으로 검색
                
                페이징
                - 무한 스크롤 방식
                - wishlist 개수 기준 내림차순 정렬
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "검색 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = SliceResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 파라미터",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = whatsinmypack.mvp.presentation.response.ApiResponse.class
                            )
                    )
            )
    })
    @GetMapping("/search")
    public SlicePackResponse searchPacks(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> contexts,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Slice<Pack> slice = searchPacksUseCase.search(q, contexts, pageable);
        Long userId = userDetails.getUserId();

        List<Pack> packs = slice.getContent();
        Set<Long> packIds = packs.stream()
                .map(Pack::getId)
                .collect(java.util.stream.Collectors.toSet());
        Set<Long> itemIds = packs.stream()
                .flatMap(pack -> pack.getPackItems().stream())
                .map(PackItem::getItem)
                .map(item -> item.getId())
                .collect(java.util.stream.Collectors.toSet());

        Set<Long> wishlistedPackIds = packIds.isEmpty()
                ? Set.of()
                : new HashSet<>(packWishListJpaRepository.findWishlistedPackIdsByUserIdAndPackIds(
                userId,
                List.copyOf(packIds)
        ));
        Set<Long> wishlistedItemIds = itemIds.isEmpty()
                ? Set.of()
                : new HashSet<>(itemWishListJpaRepository.findWishlistedItemIdsByUserIdAndItemIds(
                userId,
                List.copyOf(itemIds)
        ));

        Slice<PackDetailResponse> mapped = slice.map(pack -> PackDetailResponse.from(
                pack,
                wishlistedPackIds.contains(pack.getId()),
                wishlistedItemIds
        ));
        return SlicePackResponse.from(SliceResponse.from(mapped));
    }

    @Operation(
            summary = "인기 검색어 조회",
            description = "검색 횟수 기준으로 인기 키워드 상위 10개를 반환"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    @GetMapping("/search/popular-keywords")
    public ResponseEntity<List<String>> getPopularKeywords() {
        return ResponseEntity.ok(searchPacksUseCase.getPopularKeywords());
    }

    @Operation(
            summary = "팩 업데이트",
            description = """
                로그인한 유저가 자신의 팩을 수정
                
                수정 가능 항목:
                - 팩 소개(introduction)
                - 아이템 추가(addItems)
                - 아이템 삭제(removeItems)
                
                요청 시 addItems와 removeItems는 동시에 전달 가능하며,
                존재하지 않는 아이템 ID는 무시
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "팩 업데이트 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PackDetailResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 필요",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = whatsinmypack.mvp.presentation.response.ApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "본인 팩이 아님",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = whatsinmypack.mvp.presentation.response.ApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "팩 또는 아이템을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = whatsinmypack.mvp.presentation.response.ApiResponse.class)
                    )
            )
    })
    @PatchMapping("/{packId}")
    public PackDetailResponse updatePack(
            @PathVariable("packId") Long packId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody UpdatePackRequest request
    ) {
        Pack pack = updatePackUseCase.update(packId, userDetails.getUser(), request);
        return PackDetailResponse.from(pack);
    }

    @Operation(summary = "팩 삭제", description = "packId로 내 팩을 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "팩 없음")
    })
    @DeleteMapping("/{packId}")
    public ResponseEntity<Void> deletePack(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("packId") Long packId
    ) {
        deletePackUseCase.delete(packId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "팩 위시리스트 추가", description = "해당 팩을 위시리스트에 추가. pack_wishlists에 (user_id, pack_id) 행이 없으면 생성 후 is_wishlist=1, 있으면 is_wishlist=1로 갱신")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "팩 없음")
    })
    @PostMapping("/{packId}/wishlist")
    public ResponseEntity<Void> addWishlist(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("packId") Long packId
    ) {
        addPackWishListUseCase.add(userDetails.getUserId(), packId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "팩 위시리스트 삭제", description = "해당 팩을 위시리스트에서 제거. pack_wishlists의 is_wishlist=0으로 갱신")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @DeleteMapping("/{packId}/wishlist")
    public ResponseEntity<Void> removeWishlist(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("packId") Long packId
    ) {
        removePackWishListUseCase.remove(userDetails.getUserId(), packId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "팩 추천 조회",
            description = """
            로그인한 유저의 관심 컨텍스트 카테고리를 기준으로 팩을 추천
            
            추천 로직:
            1. 유저가 선택한 관심 컨텍스트 카테고리(최대 3개)를 조회
            2. 각 컨텍스트 카테고리별로
               - 위시리스트 개수 기준 내림차순 정렬
               - 상위 10개 팩을 조회
            3. 각 컨텍스트 카테고리별 상위 10개 중
               - 랜덤으로 3개의 팩을 선택하여 반환
            
            응답 형태:
            - Key: 컨텍스트 카테고리 ID
            - Value: 해당 카테고리에서 추천된 팩 리스트 (최대 3개)
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "팩 추천 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    example = """
                                {
                                  "1": [
                                    {
                                      "id": 10,
                                      "title": "여행 갈 때 꼭 필요한 팩",
                                      "contextCategory": "여행/문화",
                                      "nickname": "닉네임1",
                                      "items": 5,
                                      "imageUrl": "https://cdn.example.com/item/image1.jpg"
                                    },
                                    {
                                      "id": 12,
                                      "title": "기내용 미니멀 팩",
                                      "contextCategory": "여행/문화",
                                      "nickname": "닉네임2",
                                      "items": 4,
                                      "imageUrl": "https://cdn.example.com/item/image2.jpg"
                                    }
                                  ],
                                  "2": [
                                    {
                                      "id": 21,
                                      "title": "헬스장 필수 아이템",
                                      "contextCategory": "운동/건강",
                                      "nickname": "닉네임3",
                                      "items": 6,
                                      "imageUrl": "https://cdn.example.com/item/image3.jpg"
                                    }
                                  ]
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 필요",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = whatsinmypack.mvp.presentation.response.ApiResponse.class
                            )
                    )
            )
    })
    @GetMapping("/recommendation")
    public Map<Long, List<PackRecommendationResponse>> recommendPacks(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Map<Long, List<PackRecommendationResponse>> recommendations =
                getPacksUseCase.findTopByContextCategory(userDetails.getUser());

        Set<Long> packIds = recommendations.values().stream()
                .flatMap(List::stream)
                .map(PackRecommendationResponse::id)
                .collect(java.util.stream.Collectors.toSet());

        Set<Long> wishlistedPackIds = packIds.isEmpty()
                ? Set.of()
                : new HashSet<>(packWishListJpaRepository.findWishlistedPackIdsByUserIdAndPackIds(
                userDetails.getUserId(),
                List.copyOf(packIds)
        ));

        return recommendations.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(pack -> new PackRecommendationResponse(
                                        pack.id(),
                                        pack.title(),
                                        pack.contextCategory(),
                                        pack.nickname(),
                                        pack.items(),
                                        pack.imageUrl(),
                                        wishlistedPackIds.contains(pack.id())
                                ))
                                .toList()
                ));
    }

    @Operation(
            summary = "지금 뜨는 태그별 팩 조회",
            description = "컨텍스트 카테고리별로 팩을 분류하여 생성일자 최신순 상위 3개를 반환"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    @GetMapping("/trending-tags")
    public Map<String, List<PackSummaryResponse>> getTrendingTagPacks() {
        return getPacksUseCase.findLatestTop3ByContextCategory();
    }
}
