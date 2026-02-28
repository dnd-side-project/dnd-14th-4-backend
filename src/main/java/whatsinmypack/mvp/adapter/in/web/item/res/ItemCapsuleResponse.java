package whatsinmypack.mvp.adapter.in.web.item.res;

import java.util.List;
import java.util.Set;
import whatsinmypack.mvp.domain.item.entity.Item;
import whatsinmypack.mvp.domain.item.entity.value.ItemImage;
import whatsinmypack.mvp.domain.item.entity.value.ItemTag;

public record ItemCapsuleResponse(
        Long itemId,
        String title,
        String brand,
        String review,
        String satisfaction,
        String usePeriod,
        String purchase,
        List<String> imageUrls,
        List<String> tags,
        boolean isItemInWishList
) {

    public static ItemCapsuleResponse from(Item item) {
        return from(item, Set.of());
    }

    public static ItemCapsuleResponse from(Item item, Set<Long> wishlistedItemIds) {
        return new ItemCapsuleResponse(
                item.getId(),
                item.getTitle(),
                item.getBrand(),
                item.getReview(),
                item.getSatisfaction() != null ? item.getSatisfaction().kor() : null,
                item.getUsePeriod() != null ? item.getUsePeriod().kor() : "ONE_YEAR_BELOW",
                item.getPurchase(),
                item.getImages().stream()
                        .map(ItemImage::getPath)
                        .toList(),
                item.getTags().stream()
                        .map(ItemTag::getTag)
                        .toList(),
                wishlistedItemIds.contains(item.getId())
        );
    }
}