package com.gymrank.api.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "user_stats")
public class UserStats {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private AppUser user;

    private int level = 1;

    @Column(name = "exp_points")
    private int expPoints = 0;

    @Column(name = "rank_points")
    private int rankPoints = 0;

    @Column(name = "current_streak_weeks")
    private int currentStreakWeeks = 0;

    @Column(name = "best_streak_weeks")
    private int bestStreakWeeks = 0;

    @Column(name = "total_workouts")
    private int totalWorkouts = 0;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserStats() {
    }

    public UserStats(AppUser user) {
        this.user = user;
    }

    @PrePersist
    @PreUpdate
    void onSave() {
        updatedAt = Instant.now();
    }

    public int getLevel() {
        return level;
    }

    public int getExpPoints() {
        return expPoints;
    }

    public int getRankPoints() {
        return rankPoints;
    }

    public int getCurrentStreakWeeks() {
        return currentStreakWeeks;
    }

    public void addWorkoutResult(int expGain, double rankGain) {
        expPoints += Math.max(0, expGain);
        rankPoints += Math.max(0, (int) Math.round(rankGain));
        totalWorkouts += 1;
        level = Math.max(1, expPoints / 1000 + 1);
    }
}
