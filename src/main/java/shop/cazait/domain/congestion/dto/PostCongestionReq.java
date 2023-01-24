package shop.cazait.domain.congestion.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import shop.cazait.domain.cafe.entity.Cafe;
import shop.cazait.domain.congestion.entity.Congestion;
import shop.cazait.domain.congestion.entity.CongestionStatus;

@ApiModel(value = "혼잡도 등록 및 수정", description = "등록 및 수정할 혼잡도 정보")
@Data
@NoArgsConstructor
public class PostCongestionReq {

    @ApiModelProperty(value = "혼잡도 상태", required = true, example = "free")
    private String congestionStatus;

    public static Congestion toEntity(Cafe cafe, CongestionStatus congestionStatus) {
        return Congestion.builder()
                .cafe(cafe)
                .congestionStatus(congestionStatus)
                .build();
    }

}
