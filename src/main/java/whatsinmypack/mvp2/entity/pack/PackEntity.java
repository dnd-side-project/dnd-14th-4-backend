package whatsinmypack.mvp2.entity.pack;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import whatsinmypack.mvp2.entity.packComponent.PackComponentEntity;

import java.util.List;

@Entity
public class PackEntity {
    @Id
    Long id;

    String name;

    @OneToMany
    List<PackComponentEntity> components;
}
