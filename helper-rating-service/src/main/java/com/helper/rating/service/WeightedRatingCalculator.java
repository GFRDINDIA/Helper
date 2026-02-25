package com.helper.rating.service;

import com.helper.rating.entity.Rating;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Calculates time-weighted rating as per PRD Section 3.6:
 * "Weighted rating system: Recent ratings weigh more than older ones"
 *
 * Algorithm: Exponential decay weighting.
 * - Ratings from today have weight = 1.0
 * - Ratings from {decayDays} ago have weight = 0.5
 * - Older ratings decay further but never reach zero
 *
 * Formula: weight = e^(-lambda * days_ago)
 * Where lambda = ln(2) / decayDays (half-life model)
 *
 * Weighted average = sum(score * weight) / sum(weight)
 */
public class WeightedRatingCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    /**
     * Calculate time-weighted average rating.
     *
     * @param ratings    List of ratings (all visible)
     * @param decayDays  Half-life in days (e.g. 180 = ratings from 6 months ago weigh half)
     * @return Weighted average score (1.00 to 5.00), or 0 if no ratings
     */
    public static BigDecimal calculateWeightedAverage(List<Rating> ratings, int decayDays) {
        if (ratings == null || ratings.isEmpty()) return BigDecimal.ZERO;
        if (decayDays <= 0) decayDays = 180;

        double lambda = Math.log(2.0) / decayDays;
        LocalDateTime now = LocalDateTime.now();

        double sumWeightedScore = 0.0;
        double sumWeight = 0.0;

        for (Rating r : ratings) {
            long daysAgo = ChronoUnit.DAYS.between(r.getCreatedAt(), now);
            if (daysAgo < 0) daysAgo = 0;
            double weight = Math.exp(-lambda * daysAgo);
            sumWeightedScore += r.getScore() * weight;
            sumWeight += weight;
        }

        if (sumWeight == 0) return BigDecimal.ZERO;

        double weighted = sumWeightedScore / sumWeight;
        // Clamp between 1.0 and 5.0
        weighted = Math.max(1.0, Math.min(5.0, weighted));
        return BigDecimal.valueOf(weighted).setScale(SCALE, RM);
    }

    /**
     * Calculate simple average.
     */
    public static BigDecimal calculateSimpleAverage(List<Rating> ratings) {
        if (ratings == null || ratings.isEmpty()) return BigDecimal.ZERO;
        double sum = ratings.stream().mapToInt(Rating::getScore).sum();
        double avg = sum / ratings.size();
        return BigDecimal.valueOf(avg).setScale(SCALE, RM);
    }

    /**
     * Get star distribution breakdown.
     * Returns int[6] where index 1-5 has the count per star.
     */
    public static int[] getStarDistribution(List<Rating> ratings) {
        int[] dist = new int[6]; // index 0 unused
        if (ratings != null) {
            for (Rating r : ratings) {
                if (r.getScore() >= 1 && r.getScore() <= 5) {
                    dist[r.getScore()]++;
                }
            }
        }
        return dist;
    }
}
