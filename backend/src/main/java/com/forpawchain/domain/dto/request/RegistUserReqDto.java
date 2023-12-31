package com.forpawchain.domain.dto.request;

import com.forpawchain.domain.Entity.UserEntity;

import lombok.Data;

@Data
public class RegistUserReqDto {
	private String id;
	private String social;
	private String name;
	private String profile;

	public UserEntity toEntity() {
		return UserEntity.builder()
			.id(id)
			.social(social)
			.name(name)
			.profile(profile)
			.build();
	}
}
