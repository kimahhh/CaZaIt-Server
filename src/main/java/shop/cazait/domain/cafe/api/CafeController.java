package shop.cazait.domain.cafe.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import shop.cazait.domain.cafe.dto.GetCafeRes;
import shop.cazait.domain.cafe.service.CafeService;
import shop.cazait.global.common.response.BaseResponse;
import shop.cazait.global.common.status.BaseStatus;

import java.util.List;

@RestController
@RequestMapping("/api/cafes")
@RequiredArgsConstructor
public class CafeController {

    private final CafeService cafeService;

    @ResponseBody
    @GetMapping("/all")
    public BaseResponse<List<GetCafeRes>> getCafeByStatus() {
        List<GetCafeRes> cafeResList = this.cafeService.getCafeList(BaseStatus.ACTIVE);
        return new BaseResponse<>(cafeResList);
    }

    @ResponseBody
    @GetMapping("/id/{cafeId}")
    public BaseResponse<GetCafeRes> getCafeById(@PathVariable Long cafeId) {
        GetCafeRes cafeRes = this.cafeService.findCafeById(cafeId);
        return new BaseResponse<>(cafeRes);
    }

    @ResponseBody
    @GetMapping("/name/{cafeName}")
    public BaseResponse<List<GetCafeRes>> getCafeByName(@PathVariable String cafeName) {
        List<GetCafeRes> cafeResList = this.cafeService.findCafeByName(cafeName);
        return new BaseResponse<>(cafeResList);
    }
}