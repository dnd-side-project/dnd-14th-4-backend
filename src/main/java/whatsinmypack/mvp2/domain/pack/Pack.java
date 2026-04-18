package whatsinmypack.mvp2.domain.pack;

import lombok.AccessLevel;
import lombok.Getter;
import whatsinmypack.mvp2.domain.packComponent.interfaces.PackComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Pack {//Pack 순수 비즈니스 모델
    private Long id;
    private String title;
    private String introduction;

    private Long userId;
    private Long contextCategoryId;

    @Getter(AccessLevel.NONE) //components에 대해서는 getter생성x
    private List<PackComponent> components = new ArrayList<>();

    public Pack(Long id, String title, String introduction,
                Long userId, Long contextCategoryId,
                List<PackComponent> components) {

        this.id = id;
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title은 필수");
        }
        this.title = title;
        this.introduction = introduction;
        this.userId = userId;
        this.contextCategoryId = contextCategoryId;
        this.components = (components != null)
                ? new ArrayList<>(components) //방어적 복사
                : new ArrayList<>();
    }

    //읽기 전용 get
    public List<PackComponent> getComponents() {
        return Collections.unmodifiableList(components);//수정 방지
    }

    //팩 내부 컴포넌트 추가
    public void addComponent(PackComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("component는 null이 될 수 없음");
        }
        components.add(component);
    }

    //팩 내부 컴포넌트 삭제
    public void removeComponent(PackComponent component) {
        components.remove(component);
    }

}