package com.forpawchain.auth;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import com.forpawchain.domain.Entity.JwtExpirationEnums;
import com.forpawchain.domain.dto.response.TokenResDto;

@Slf4j
@Component
public class JwtTokenProvider {
	private final Key key;

	// jwt.secret을 통해 secret key 생성
	public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	// 유저 정보를 통해 accessToken, refreshToken 생성
	public TokenResDto generateToken(Authentication authentication) {
		// 권한
		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		// TO DO
		if(authorities.equals("")) {
			authorities = "ROLE_USER";
		}

		long now = (new Date()).getTime(); // 현재 시간

		// accessToken
		String accessToken = Jwts.builder()
			.setSubject(authentication.getName())
			.claim("auth", authorities)
			.setExpiration(new Date(now + JwtExpirationEnums.ACCESS_TOKEN_EXPIRATION_TIME.getValue()))
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();

		// refreshToken
		String refreshToken = Jwts.builder()
			.setExpiration(new Date(now + JwtExpirationEnums.REFRESH_TOKEN_EXPIRATION_TIME.getValue()))
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();

		return new TokenResDto("Bearer", accessToken, refreshToken);
	}

	// 토큰 정보 조회
	public Authentication getAuthentication(String accessToken) {
		Claims claims = parseClaims(accessToken);
		if(claims.get("auth") == null) {
			throw new RuntimeException("권한 정보가 없는 토큰입니다.");
		}

		// 권한 정보 조회
		Collection<? extends GrantedAuthority> authorities =
			Arrays.stream(claims.get("auth").toString().split(","))
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		// Authentication을 UserDetails 객체에 할당
		UserDetails principal =
			new User(claims.getSubject(), "", authorities);

		return new UsernamePasswordAuthenticationToken(principal, "", authorities);
	}

	// 토큰 복호화
	private Claims parseClaims(String accessToken) {
		try {
			return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(accessToken)
				.getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}

	// 토큰에서 식별자 변환
	public String getUserId(String token) {
		return Jwts.parser()
			.setSigningKey(key)
			.parseClaimsJws(token)
			.getBody()
			.getSubject();
	}

	// 토큰 유효성 검증
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().
				setSigningKey(key)
				.build()
				.parseClaimsJws(token);
			return true;
		} catch (io.jsonwebtoken.security.SignatureException | MalformedJwtException e) {
			// TO DO: 형식화된 예외 처리
			log.info("Invalid JWT Token", e);
			throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
		} catch (ExpiredJwtException e) {
			log.info("Expired JWT Token", e);
			throw new IllegalArgumentException("만료된 토큰입니다.");
		} catch (UnsupportedJwtException e) {
			log.info("Unsupported JWT Token", e);
			throw new IllegalArgumentException("지원하지 않는 토큰 형식입니다.");
		} catch (IllegalArgumentException e) {
			log.info("JWT claims string is empty.", e);
			throw new IllegalArgumentException("빈 토큰입니다.");
		}
		// return false;
	}

	public long getRemainMilliSeconds(String token) {
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token)
			.getBody();
		Date expiration = claims.getExpiration();
		Date now = new Date();

		return expiration.getTime() - now.getTime();
	}
}