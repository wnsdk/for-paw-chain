package com.forpawchain.domain.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JwtExpirationEnums {
	ACCESS_TOKEN_EXPIRATION_TIME("JWT 만료 시간 / 7일", 1000L * 60 * 60 * 24 * 7),
	REFRESH_TOKEN_EXPIRATION_TIME("Refresh 토큰 만료 시간 / 7일", 1000L * 60 * 60 * 24 * 7),
	REISSUE_EXPIRATION_TIME("Refresh 토큰 만료 시간 / 7일", 1000L * 60 * 60 * 24 * 7);

	private String description;
	private Long value;
}