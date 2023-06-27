package shop.cazait.domain.master.api;

import static shop.cazait.global.error.status.SuccessStatus.*;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import shop.cazait.domain.master.dto.request.MasterCreateInDTO;
import shop.cazait.domain.master.dto.request.MasterUpdateInDTO;
import shop.cazait.domain.master.dto.response.MasterCreateOutDTO;
import shop.cazait.domain.master.error.MasterException;
import shop.cazait.domain.master.service.MasterService;
import shop.cazait.domain.user.exception.UserException;
import shop.cazait.global.common.dto.response.SuccessResponse;
import shop.cazait.global.config.encrypt.JwtService;
import shop.cazait.global.config.encrypt.NoAuth;

@Tag(name = "마스터 API")
@Validated
@RestController
@RequestMapping("/api/masters")
@RequiredArgsConstructor
public class MasterController {

	private final MasterService masterService;
	private final JwtService jwtService;

	@NoAuth
	@PostMapping("/sign-up")
	@Operation(summary = "마스터 회원가입", description = "마스터 사용자의 정보들을 이용해서 회원가입을 진행한다.")
	public SuccessResponse<MasterCreateOutDTO> registerMaster(@Validated @RequestBody MasterCreateInDTO dto) throws
		MasterException,
		InvalidAlgorithmParameterException,
		NoSuchPaddingException,
		IllegalBlockSizeException,
		NoSuchAlgorithmException,
		BadPaddingException,
		InvalidKeyException {
		MasterCreateOutDTO postCreateMasterRes = masterService.registerMaster(dto);
		return new SuccessResponse<>(CREATE_MASTER, postCreateMasterRes);
	}

	@PatchMapping("/{masterId}")
	@Operation(summary = "마스터 정보 수정", description = "특정 ID의 마스터 관련 정보를 수정한다.")
	@Parameters({
		@Parameter(name = "masterId", description = "response로 발급 받은 계정 마스터 ID 번호", example = "1"),
		@Parameter(name = "REFRESH-TOKEN", description = "발급 받은 refreshtoken")}
	)
	public SuccessResponse<String> updateMaster(
		@PathVariable(name = "masterId") Long masterId,
		@RequestBody @Valid MasterUpdateInDTO masterUpdateInDTO,
		@RequestHeader(value = "REFRESH-TOKEN") String refreshToken) throws UserException {
		jwtService.isValidAccessTokenId(masterId);
		masterService.updateMaster(masterId, masterUpdateInDTO);
		return new SuccessResponse<>(SUCCESS, "마스터 정보 수정 완료");
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "마스터 계정 탈퇴", description = "특정 ID의 마스터 계정을 삭제한다.")
	@Parameter(name = "masterId", description = "탈퇴하고자 하는 마스터 ID 번호", example = "1")
	public SuccessResponse<String> deleteMaster(@Validated @PathVariable Long id) throws MasterException {
		masterService.removeMaster(id);
		String response = "회원 탈퇴가 성공하였습니다.";
		return new SuccessResponse<>(SUCCESS, response);
	}

}
