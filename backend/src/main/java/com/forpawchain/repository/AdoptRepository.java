package com.forpawchain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.forpawchain.domain.dto.response.AdoptDetailResDto;
import com.forpawchain.domain.dto.response.AdoptListResDto;
import com.forpawchain.domain.Entity.AdoptEntity;

public interface AdoptRepository extends JpaRepository<AdoptEntity, String> {

	@Query(value = "SELECT a.pid, a.profile, pr.type, pr.kind, pr.spayed\n"
		+ "FROM adopt a, pet_reg pr\n"
		+ "WHERE a.pid = pr.pid\n"
		+ "ORDER BY RAND() \n"
		+ "LIMIT 10", nativeQuery = true)
	Optional<List<AdoptListResDto>> findTop10ByRand();

	Optional<AdoptEntity> findByPid(String pid);

	@Query(value = "SELECT a.pid, a.profile, pr.type, pr.kind, pr.spayed\n"
		+ "FROM adopt a, pet_reg pr\n"
		+ "WHERE a.pid = pr.pid and a.uid = :uid", nativeQuery = true)
	List<AdoptListResDto> findByUid(@Param("uid") Long uid);

	@Query(value = "SELECT pr.name, pr.sex, a.profile, pr.type, pr.kind, pr.spayed, a.tel, a.etc\n"
		+ "FROM adopt a, pet_reg pr\n"
		+ "WHERE a.pid = pr.pid and a.pid = :pid", nativeQuery = true)
	Optional<AdoptDetailResDto> findDetailByPid(@Param("pid") String pid);

	void deleteByPid(String pid);

	@Query(value = "SELECT a.pid, a.profile, pr.type, pr.kind, pr.spayed\n"
		+ "FROM adopt a, pet_reg pr\n"
		+ "WHERE a.pid = pr.pid and pr.type LIKE COALESCE(:type, '%')\n"
		+ "and pr.sex LIKE COALESCE(:sex, '%')\n"
		+ "and pr.spayed = :spayed"
		, nativeQuery = true)
	Optional<PageImpl<AdoptListResDto>> findByTypeAndSexAndSpayed(@Param("type") String type, @Param("sex") String sex,
		@Param("spayed") int spayed, PageRequest pageRequest);

	@Query(value = "SELECT a.pid, a.profile, pr.type, pr.kind, pr.spayed\n"
		+ "FROM adopt a, pet_reg pr\n"
		+ "WHERE a.pid = pr.pid and pr.type LIKE COALESCE(:type, '%')\n"
		+ "and pr.sex LIKE COALESCE(:sex, '%')"
		, nativeQuery = true)
	Optional<PageImpl<AdoptListResDto>> findByTypeAndSex(@Param("type") String type, @Param("sex") String sex, PageRequest pageRequest);
}
