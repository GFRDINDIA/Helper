package com.helper.rating.repository;

import com.helper.rating.entity.Rating;
import com.helper.rating.enums.RatingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {

    // Check if user already rated for this task in this direction
    boolean existsByTaskIdAndGivenByAndGivenTo(UUID taskId, UUID givenBy, UUID givenTo);

    // All ratings received by a user (visible only)
    Page<Rating> findByGivenToAndIsVisibleTrueOrderByCreatedAtDesc(UUID givenTo, Pageable pageable);

    // All ratings given by a user
    Page<Rating> findByGivenByOrderByCreatedAtDesc(UUID givenBy, Pageable pageable);

    // Ratings for a specific task
    List<Rating> findByTaskId(UUID taskId);

    // Count visible ratings for a user
    long countByGivenToAndIsVisibleTrue(UUID givenTo);

    // Simple average
    @Query("SELECT COALESCE(AVG(r.score), 0) FROM Rating r WHERE r.givenTo = :uid AND r.isVisible = true")
    BigDecimal findAverageScoreByGivenTo(@Param("uid") UUID userId);

    // All visible ratings for weighted calculation (ordered by date DESC)
    @Query("SELECT r FROM Rating r WHERE r.givenTo = :uid AND r.isVisible = true ORDER BY r.createdAt DESC")
    List<Rating> findVisibleRatingsForUser(@Param("uid") UUID userId);

    // Star breakdown
    @Query("SELECT r.score, COUNT(r) FROM Rating r WHERE r.givenTo = :uid AND r.isVisible = true GROUP BY r.score")
    List<Object[]> findScoreDistribution(@Param("uid") UUID userId);

    // Recent ratings within time window (for checking if user can still rate)
    Optional<Rating> findByTaskIdAndGivenBy(UUID taskId, UUID givenBy);

    // Admin: all ratings with optional filter
    Page<Rating> findByRatingType(RatingType type, Pageable pageable);

    // Admin stats
    @Query("SELECT COALESCE(AVG(r.score), 0) FROM Rating r WHERE r.isVisible = true")
    BigDecimal findPlatformAverageRating();

    long countByIsVisibleTrue();

    long countByIsVisibleFalse();
}
