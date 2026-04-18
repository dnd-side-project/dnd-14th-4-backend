package whatsinmypack.mvp2.entity.packComponent;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public class PackComponentEntity {
    @Id
    Long id;

    Long packId;
}
