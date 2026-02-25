package com.helper.rating;

import com.helper.rating.entity.Rating;
import com.helper.rating.enums.RatingType;
import com.helper.rating.service.WeightedRatingCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeightedRatingCalculatorTest {

    private Rating makeRating(int score, int daysAgo) {
        return Rating.builder()
                .score(score)
                .ratingType(RatingType.CUSTOMER_TO_WORKER)
                .createdAt(LocalDateTime.now().minusDays(daysAgo))
                .build();
    }

    @Test
    @DisplayName("Simple average: all 5 stars")
    void testAllFiveStars() {
        List<Rating> ratings = List.of(makeRating(5, 0), makeRating(5, 1), makeRating(5, 2));
        BigDecimal avg = WeightedRatingCalculator.calculateSimpleAverage(ratings);
        assertEquals(new BigDecimal("5.00"), avg);
    }

    @Test
    @DisplayName("Simple average: mixed scores")
    void testMixedScores() {
        List<Rating> ratings = List.of(makeRating(5, 0), makeRating(4, 1), makeRating(3, 2));
        BigDecimal avg = WeightedRatingCalculator.calculateSimpleAverage(ratings);
        assertEquals(new BigDecimal("4.00"), avg);
    }

    @Test
    @DisplayName("Simple average: empty list returns zero")
    void testEmptyListSimple() {
        assertEquals(BigDecimal.ZERO, WeightedRatingCalculator.calculateSimpleAverage(Collections.emptyList()));
    }

    @Test
    @DisplayName("Simple average: null returns zero")
    void testNullSimple() {
        assertEquals(BigDecimal.ZERO, WeightedRatingCalculator.calculateSimpleAverage(null));
    }

    @Test
    @DisplayName("Weighted: recent ratings weigh more")
    void testRecentWeighMore() {
        // Recent 5-star + old 1-star → weighted should be closer to 5 than simple avg of 3
        List<Rating> ratings = List.of(makeRating(5, 0), makeRating(1, 365));
        BigDecimal weighted = WeightedRatingCalculator.calculateWeightedAverage(ratings, 180);
        BigDecimal simple = WeightedRatingCalculator.calculateSimpleAverage(ratings);
        assertTrue(weighted.compareTo(simple) > 0,
                "Weighted (" + weighted + ") should be > simple (" + simple + ")");
    }

    @Test
    @DisplayName("Weighted: all same day → equals simple average")
    void testSameDayEqualsSimple() {
        List<Rating> ratings = List.of(makeRating(4, 0), makeRating(3, 0), makeRating(5, 0));
        BigDecimal weighted = WeightedRatingCalculator.calculateWeightedAverage(ratings, 180);
        BigDecimal simple = WeightedRatingCalculator.calculateSimpleAverage(ratings);
        assertEquals(simple, weighted);
    }

    @Test
    @DisplayName("Weighted: empty list returns zero")
    void testEmptyListWeighted() {
        assertEquals(BigDecimal.ZERO,
                WeightedRatingCalculator.calculateWeightedAverage(Collections.emptyList(), 180));
    }

    @Test
    @DisplayName("Weighted: single rating")
    void testSingleRating() {
        List<Rating> ratings = List.of(makeRating(4, 10));
        BigDecimal weighted = WeightedRatingCalculator.calculateWeightedAverage(ratings, 180);
        assertEquals(new BigDecimal("4.00"), weighted);
    }

    @Test
    @DisplayName("Star distribution: correct counts")
    void testStarDistribution() {
        List<Rating> ratings = List.of(
                makeRating(5, 0), makeRating(5, 0), makeRating(4, 0),
                makeRating(3, 0), makeRating(1, 0));
        int[] dist = WeightedRatingCalculator.getStarDistribution(ratings);
        assertEquals(1, dist[1]); // 1 star
        assertEquals(0, dist[2]); // 2 star
        assertEquals(1, dist[3]); // 3 star
        assertEquals(1, dist[4]); // 4 star
        assertEquals(2, dist[5]); // 5 star
    }

    @Test
    @DisplayName("Star distribution: empty list")
    void testEmptyDistribution() {
        int[] dist = WeightedRatingCalculator.getStarDistribution(null);
        for (int i = 1; i <= 5; i++) assertEquals(0, dist[i]);
    }

    @Test
    @DisplayName("Weighted: very old ratings have minimal impact")
    void testVeryOldRatings() {
        // 1-star from 2 years ago + 5-star today → should be very close to 5
        List<Rating> ratings = List.of(makeRating(5, 0), makeRating(1, 730));
        BigDecimal weighted = WeightedRatingCalculator.calculateWeightedAverage(ratings, 180);
        assertTrue(weighted.compareTo(new BigDecimal("4.50")) > 0,
                "Weighted (" + weighted + ") should be > 4.50 (old 1-star nearly ignored)");
    }

    @Test
    @DisplayName("Weighted: result clamped between 1.0 and 5.0")
    void testClampedRange() {
        List<Rating> ratings = List.of(makeRating(1, 0));
        BigDecimal weighted = WeightedRatingCalculator.calculateWeightedAverage(ratings, 180);
        assertTrue(weighted.compareTo(new BigDecimal("1.00")) >= 0);
        assertTrue(weighted.compareTo(new BigDecimal("5.00")) <= 0);
    }
}
