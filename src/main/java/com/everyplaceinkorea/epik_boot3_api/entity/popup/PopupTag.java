package com.everyplaceinkorea.epik_boot3_api.entity.popup;

import com.everyplaceinkorea.epik_boot3_api.admin.contents.popup.dto.PopupTagDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@ToString
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopupTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "popup_id", foreignKey = @ForeignKey(name = "FK_POPUP_TAG_POPUP"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Popup popup;

    public void updatePopupTag(PopupTagDto popupTagDto) {
        this.tag = popupTagDto.getTag();
    }

    public void addPopup(Popup popup) {
        this.popup = popup;
    }

    @Builder
    public PopupTag(String tag, Popup popup) {
        this.tag = tag;
        this.popup = popup;
    }
}
