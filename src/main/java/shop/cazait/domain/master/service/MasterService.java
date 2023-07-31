package shop.cazait.domain.master.service;

import static shop.cazait.global.error.status.ErrorStatus.*;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import shop.cazait.domain.auth.dto.UserAuthenticateInDTO;
import shop.cazait.domain.auth.dto.UserAuthenticateOutDTO;
import shop.cazait.domain.master.dto.request.MasterCreateInDTO;
import shop.cazait.domain.master.dto.request.MasterUpdateInDTO;
import shop.cazait.domain.master.dto.response.MasterCreateOutDTO;
import shop.cazait.domain.master.dto.response.MasterListOutDTO;
import shop.cazait.domain.master.dto.response.MasterUptateOutDTO;

import shop.cazait.domain.master.entity.Master;
import shop.cazait.domain.master.error.MasterException;
import shop.cazait.domain.master.repository.MasterRepository;
import shop.cazait.domain.user.exception.UserException;
import shop.cazait.global.common.status.BaseStatus;
import shop.cazait.global.config.encrypt.AES128;
import shop.cazait.global.config.encrypt.JwtService;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MasterService {

	private final MasterRepository masterRepository;
	private final JwtService jwtService;

	@Value("${password-secret-key}")
	private String PASSWORD_SECRET_KEY;

	/**
	 * 마스터 회원 가입
	 */
	public MasterCreateOutDTO registerMaster(MasterCreateInDTO dto) throws
		MasterException,
		InvalidAlgorithmParameterException,
		NoSuchPaddingException,
		IllegalBlockSizeException,
		NoSuchAlgorithmException,
		BadPaddingException,
		InvalidKeyException {

		//아이디 중복확인
		if (masterRepository.findMasterByAccountName(dto.getAccountName()).isPresent()) {
			throw new MasterException(EXIST_ACCOUNTNAME);
		}

		//휴대전화번호 중복확인
		if (masterRepository.findByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
			throw new MasterException(EXIST_PHONENUMBER);
		}

		//닉네임 중복확인
		if (masterRepository.findMasterByNickname(dto.getNickname()).isPresent()) {
			throw new MasterException(EXIST_NICKNAME);
		}

		//비밀번호 암호화
		String encryptedMasterPassword = encryptPassword(dto.getPassword());
		MasterCreateInDTO masterCreateInDTO = dto.encryptMasterCreateDTO(encryptedMasterPassword);

		Master master = MasterCreateInDTO.toEntity(masterCreateInDTO);
		masterRepository.save(master);

		return MasterCreateOutDTO.of(master);

	}

	/**
	 * 마스터 로그인
	 */
	public UserAuthenticateOutDTO LoginMaster(UserAuthenticateInDTO userAuthenticateInDTO) throws
		MasterException,
		InvalidAlgorithmParameterException,
		NoSuchPaddingException,
		IllegalBlockSizeException,
		NoSuchAlgorithmException,
		BadPaddingException,
		InvalidKeyException {

		Master findMaster = masterRepository.findMasterByAccountName(userAuthenticateInDTO.getAccountName())
				.orElseThrow(() -> new MasterException(FAILED_TO_LOGIN));

		//DB에 있는 암호화된 비밀번호
		String findMasterPassword = findMaster.getPassword();
		//로그인 시 입력한 비밀번호를 암호화
		String loginPassword = encryptPassword(userAuthenticateInDTO.getPassword());


		if (findMasterPassword.equals(loginPassword)){
			//토큰 생성
			UUID masterIdx = findMaster.getId();
			String accessToken = jwtService.createJwt(masterIdx);
			String refreshToken = jwtService.createRefreshToken();

			//리프레시 토큰 추가 후 저장
			findMaster.loginMaster(refreshToken);
			masterRepository.save(findMaster);

			return UserAuthenticateOutDTO.of(findMaster, accessToken);
		}
		throw new MasterException(FAILED_TO_LOGIN);
	}

	//마스터 회원 전체 조회
	@Transactional(readOnly = true)
	public List<MasterListOutDTO> getMasterByStatus(BaseStatus status) throws MasterException {
		List<Master> masterList = masterRepository.findMasterByStatus(status);
		List<MasterListOutDTO> masterResList = new ArrayList<>();
		for (Master master : masterList) {
			MasterListOutDTO masterRes = MasterListOutDTO.of(master);
			masterResList.add(masterRes);
		}
		if (masterResList.isEmpty()) {
			throw new MasterException(NOT_EXIST_MASTER);
		}
		return masterResList;
	}

	//마스터 회원 정보 업데이트
	public MasterUptateOutDTO updateMaster(UUID id, MasterUpdateInDTO masterUpdateInDTO) throws MasterException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
		Master master = masterRepository.findMasterById(id)
				.orElseThrow(()->new MasterException(NOT_EXIST_MASTER));

		String encryptPassword = encryptPassword(masterUpdateInDTO.getPassword());
		MasterUpdateInDTO encryptedUserUpdateDTO = masterUpdateInDTO.encryptMasterUpdateDTO(encryptPassword);

		Master updatedMaster = master.updateMasterProfile(encryptedUserUpdateDTO);
		return MasterUptateOutDTO.of(updatedMaster);
	}

	// 회원 탈퇴하기
	public void removeMaster(UUID id) throws MasterException {
		Master master = masterRepository.findMasterById(id).get();

		if (masterRepository.findMasterById(id).isEmpty()) {
			throw new MasterException(NOT_EXIST_MASTER);
		}

		masterRepository.delete(master);
	}

	public String encryptPassword(String password) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
		return new AES128(PASSWORD_SECRET_KEY).encrypt(password);
	}

	// 토큰 재발급
	public UserAuthenticateOutDTO issueAccessToken(String accessToken, String refreshToken) throws
		MasterException,
		UserException {

		Master master = null;
		UUID masterIdx = null;

		log.info("accessToken = " + accessToken);
		log.info("refreshToken = " + refreshToken);

		if (jwtService.isValidAccessTokenInRefresh(accessToken)) {
			log.info("아직 accesstoken 유효");
			throw new MasterException(NOT_EXPIRED_TOKEN);
		} else {
			log.info("Access 토큰 만료됨");
			if (jwtService.isValidRefreshTokenInRefresh(refreshToken)) {     //들어온 Refresh 토큰이 유효한지
				log.info("아직 refreshtoken 유효함");
				masterIdx = jwtService.getUserIdx(accessToken);
				master = masterRepository.findById(masterIdx).get();
				String tokenFromDB = master.getRefreshToken();
				log.info("userIdx from accessToken: " + masterIdx);
				log.info("refreshToken found by accessToken(userIdx): " + tokenFromDB);

				if (refreshToken.equals(tokenFromDB)) {
					log.info("Access token 재발급");
					accessToken = jwtService.createJwt(masterIdx);
				} else {
					log.error("Refresh Token Tampered, not equal from db refreshtoken");
					throw new MasterException(INVALID_JWT);
				}
			} else {
				log.info("refresh token 재발급");
				masterIdx = jwtService.getUserIdx(accessToken);
				master = masterRepository.findById(masterIdx).get();
				accessToken = jwtService.createJwt(masterIdx);
				refreshToken = jwtService.createRefreshToken();
			}
		}
		return UserAuthenticateOutDTO.of(master, accessToken);
	}

}