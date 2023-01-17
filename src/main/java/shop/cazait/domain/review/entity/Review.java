package shop.cazait.domain.review.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.cazait.domain.cafe.entity.Cafe;
import shop.cazait.domain.review.dto.PatchReviewReq;
import shop.cazait.domain.user.entity.User;
import shop.cazait.global.common.entity.BaseEntity;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    private Cafe cafe;

    @Column(nullable = false)
    private int score;

    private String content;


    @Builder
    public Review(User user, Cafe cafe, int score, String content) {
        this.user = user;
        this.cafe = cafe;
        this.score = score;
        this.content = content;
    }

    public Review update(PatchReviewReq patchReviewReq) {
        this.score = patchReviewReq.getScore();
        this.content = patchReviewReq.getContent();

        return this;
    }

    /**
     * 추후 확장
     *@NonNull
     *private int seat;
     *@NonNull
     *private int cleanliness;
     */
}
