package io.mosip.resident.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.resident.entity.ResidentUserEntity;

/**
 * The Interface ResidentUserRepository.
 * 
 * @author Neha Farheen
 * @since 1.2.0.1
 */
@Transactional
@Repository
public interface ResidentUserRepository extends JpaRepository<ResidentUserEntity, String> {
	Optional<ResidentUserEntity> findById(String id);
	
	List<ResidentUserEntity> findFirst2ByIdaTokenOrderByLoginDtimesDesc(String idaToken);

	@Modifying
	@Query("update ResidentUserEntity res set res.lastbellnotifDtimes =:datetime where res.sessionId =:sessionId")
	int updateByIdLastbellnotifDtimes(@Param("sessionId") String sessionId, @Param("datetime") LocalDateTime datetime);
	
	Optional<ResidentUserEntity> findFirstByIdaTokenOrderByLoginDtimesDesc(String idaToken);
}
