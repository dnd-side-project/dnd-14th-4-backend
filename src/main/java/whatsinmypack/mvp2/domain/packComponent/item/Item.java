package whatsinmypack.mvp2.domain.packComponent.item;

import whatsinmypack.mvp2.domain.packComponent.interfaces.PackComponent;

public class Item implements PackComponent {
    Long id;
    String title;
    public Long getId() { return id; }
    public String getLabel() { return title; }
}
