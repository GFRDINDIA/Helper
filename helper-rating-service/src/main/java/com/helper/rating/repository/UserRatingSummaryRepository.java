package com.helper.rating.repository;

import com.helper.rating.entity.UserRatingSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRatingSummaryRepository extends JpaRepository<UserRatingSummary, UUID> {

    // Top rated users (for leaderboard / search ranking)
    @Query("SELECT s FROM UserRatingSummary s WHERE s.isPublic = true ORDER BY s.weightedRating DESC")
    List<UserRatingSummary> findTopRatedUsers(org.springframework.data.domain.Pageable pageable);
}
