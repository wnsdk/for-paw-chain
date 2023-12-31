package com.forpawchain.repository;

import com.forpawchain.domain.Entity.AuthenticationEntity;
import com.forpawchain.domain.Entity.AuthenticationId;
import com.forpawchain.domain.Entity.AuthenticationType;
import com.forpawchain.domain.dto.response.UserResDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AuthenticationRepository extends JpaRepository<AuthenticationEntity, AuthenticationId> {
    Optional<AuthenticationEntity> findByAuthId(AuthenticationId id);
    Optional<AuthenticationEntity> findByAuthIdUidAndAuthIdPid(long uid, String pid);
    /**
     * 회원과 동물에 대한 관계 찾기
     * @param uid
     * @param pid
     * @return AuthenticationEntity의 type
     */
    @Query("select a.type from AuthenticationEntity a \n" +
            "where a.authId.uid = :uid and a.authId.pid = :pid")
    String findTypeByAuthIdUidAndAuthIdPid(@Param("uid") long uid, @Param("pid") String pid);
    List<AuthenticationEntity> findAllByAuthIdUid(long uid);
    List<AuthenticationEntity> findAllByAuthIdPid(String pid);
    void deleteByAuthIdUidAndAuthIdPid(long uid, String pid);
    @Query(value = "select a.uid uid, u.name name, u.profile profile \n" +
            "from authentication a join user u on a.uid = u.uid \n"+
            "where a.pid = :pid and a.type != 'MASTER'", nativeQuery = true)
    List<UserResDto> findUserAllByPid(@Param("pid") String pid);
    // 동물 pid에 대한 uid사람의 권한 반환
    @Query("select a.type from AuthenticationEntity a " +
            "where pid = :pid and uid = :uid")
    AuthenticationType findAuthenticationTypeByUidAndPid(@Param("uid") long uid, @Param("pid") String pid);

    // 동물 pid에 대한 권한이 type인 사람의 uid 반환
    @Query(value = "select a.authId.uid from AuthenticationEntity a " +
            "where pid = :pid and type = :type", nativeQuery = false)
    Optional<Long> findUidByPidAndType(@Param("pid") String pid, @Param("type") AuthenticationType type);

    @Query(value = "select a.regTime from AuthenticationEntity a " +
            "where uid = :uid and pid = :pid", nativeQuery = false)
    LocalDate findRegDateByAuthIdUidAndAuthIdPid(long uid, String pid);
}
