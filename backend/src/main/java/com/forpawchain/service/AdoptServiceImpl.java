package com.forpawchain.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.forpawchain.domain.dto.request.AdoptDetailReqDto;
import com.forpawchain.domain.dto.response.AdoptDetailResDto;
import com.forpawchain.domain.dto.response.AdoptListResDto;
import com.forpawchain.domain.entity.AdoptEntity;
import com.forpawchain.domain.entity.PetEntity;
import com.forpawchain.domain.entity.UserEntity;
import com.forpawchain.repository.AdoptRepository;
import com.forpawchain.repository.PetRepository;
import com.forpawchain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class AdoptServiceImpl implements AdoptService {

	private final AdoptRepository adoptRepository;
	private final UserRepository userRepository;
	private final PetRepository petRepository;

	@Override
	public Page<AdoptListResDto> getAdoptList(int pageNo, String type, Integer spayed, String sex) {
		PageRequest pageRequest = PageRequest.of(pageNo, 10);
		Page<AdoptListResDto> adoptListResDtos = null;

		// 전체 검색
		// if (type.isBlank() && spayed == null && sex.isBlank() ) {
		// 	adoptListResDtos = adoptRepository.findAll(pageRequest);
		// }


		return adoptListResDtos;
	}

	@Override
	public List<AdoptListResDto> getAdoptAd() {
		List<AdoptListResDto> list = adoptRepository.findTop10ByRand();
		return list;
	}

	@Override
	public AdoptDetailResDto getAdoptDetail(String pid) {

		AdoptDetailResDto adoptDetailResDto = adoptRepository.findDetailByPid(pid);

		return adoptDetailResDto;
	}

	@Override
	public void registAdopt(AdoptDetailReqDto adoptDetailReqDto, Long uid) {

		String pid = adoptDetailReqDto.getPid();
		PetEntity petEntity = petRepository.findByPid(pid);
		UserEntity userEntity = userRepository.findByUid(uid);

		// 유기견 추가
		AdoptEntity adoptEntity = AdoptEntity.builder()
			.pid(adoptDetailReqDto.getPid())
			.uid(uid)
			.profile1(adoptDetailReqDto.getProfile1())
			.profile2(adoptDetailReqDto.getProfile2())
			.tel(adoptDetailReqDto.getTel())
			.etc(adoptDetailReqDto.getEtc())
			.pet(petEntity)
			.user(userEntity)
			.build();

		if (adoptDetailReqDto.getProfile2() != null) {
			adoptEntity.setProfile2(adoptDetailReqDto.getProfile2());
		}

		adoptRepository.save(adoptEntity);
	}

	@Override
	public void modifyAdopt(AdoptDetailReqDto adoptDetailReqDto) {
		String pid = adoptDetailReqDto.getPid();
		AdoptEntity adoptEntity = adoptRepository.findByPid(pid);

		adoptEntity.setProfile1(adoptDetailReqDto.getProfile1());
		adoptEntity.setProfile2(adoptDetailReqDto.getProfile2());
		adoptEntity.setEtc(adoptDetailReqDto.getEtc());
		adoptEntity.setTel(adoptDetailReqDto.getTel());
	}

	@Override
	public void removeAdopt(String pid) {
		// Pet의 lost 여부 변경
		PetEntity petEntity = petRepository.findByPid(pid);
		petEntity.setLost(false);
		petRepository.save(petEntity);

		// 분양 공고 삭제
		adoptRepository.deleteByPid(pid);
	}

	@Override
	public List<AdoptListResDto> getAdoptMyList(Long uid) {
		List<AdoptListResDto> list = adoptRepository.findByUid(uid);
		return list;
	}
}
