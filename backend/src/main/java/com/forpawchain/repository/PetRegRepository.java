package com.forpawchain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.forpawchain.domain.dto.response.PetDefaultInfoResDto;
import com.forpawchain.domain.dto.response.PetInfoResDto;
import com.forpawchain.domain.Entity.PetRegEntity;

public interface PetRegRepository extends JpaRepository<PetRegEntity, String> {
	@Query(value = "SELECT r.pid AS pid, r.name AS name, r.sex AS sex, r.kind AS kind, r.type AS type, r.spayed AS spayed,\n"
		+ "i.birth AS birth, i.etc AS etc, i.profile AS profile, i.region AS region, i.tel AS tel\n"
		+ "FROM pet_reg r LEFT OUTER JOIN pet_info i ON i.pid = :pid WHERE r.pid = :pid", nativeQuery = true)
	PetInfoResDto findRegAndInfoByPid(@Param("pid")String pid);

	@Query(value = "SELECT r.pid AS pid, r.name AS name, r.sex AS sex, r.kind AS kind, r.type AS type, r.spayed AS spayed, i.birth AS birth, i.etc AS etc, i.profile AS profile, i.region AS region, i.tel AS tel\n"
		+ "FROM (SELECT pid, name, sex, kind, type, spayed FROM pet_reg WHERE pid\n"
		+ "IN (SELECT a.pid FROM authentication a WHERE a.uid = :userId)) r\n"
		+ "LEFT OUTER JOIN\n"
		+ "(SELECT pid, birth, etc, profile, region, tel FROM pet_info WHERE pid \n"
		+ "IN (SELECT a.pid FROM authentication a WHERE a.uid = :userId)) i\n"
		+ "ON r.pid = i.pid", nativeQuery = true)
	List<PetDefaultInfoResDto> findAuthAndInfo(@Param("userId") Long uid);

	PetRegEntity findByPid(String pid);
}
