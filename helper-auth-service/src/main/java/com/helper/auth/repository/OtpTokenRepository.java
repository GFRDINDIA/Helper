package com.helper.auth.repository;

import com.helper.auth.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {

    Optional<OtpToken> findTopByEmailAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(
            String email, String purpose);

    @Modifying
    @Query("UPDATE OtpToken o SET o.isUsed = true WHERE o.email = :email AND o.purpose = :purpose")
    void invalidateAllByEmailAndPurpose(@Param("email") String email, @Param("purpose") String purpose);

    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.expiresAt < :now OR o.isUsed = true")
    void deleteExpiredAndUsed(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(o) FROM OtpToken o WHERE o.email = :email AND o.purpose = :purpose " +
            "AND o.createdAt > :since AND o.isUsed = false")
    long countRecentOtpsByEmail(@Param("email") String email, @Param("purpose") String purpose,
                                @Param("since") LocalDateTime since);
}
