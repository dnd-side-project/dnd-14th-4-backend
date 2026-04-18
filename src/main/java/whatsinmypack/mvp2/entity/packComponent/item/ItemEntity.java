package whatsinmypack.mvp2.entity.packComponent.item;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import whatsinmypack.mvp2.entity.packComponent.PackComponentEntity;

@Entity
@DiscriminatorValue("ITEM")
public class ItemEntity extends PackComponentEntity {
    String title;
}
