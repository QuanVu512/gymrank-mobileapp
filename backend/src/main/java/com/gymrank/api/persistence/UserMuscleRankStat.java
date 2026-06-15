package com.gymrank.api.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "user_muscle_rank_stats",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_muscle_rank_stats_user_muscle", columnNames = {"user_id", "muscle_code"}),
        indexes = @Index(name = "idx_user_muscle_rank_stats_user_id", columnList = "user_id")
)
public class UserMuscleRankStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "muscle_code", nullable = false, length = 60)
    private String muscleCode;

    @Column(name = "rank_points", nullable = false)
    private double rankPoints = 0d;

    @Column(name = "last_trained_at")
    private Instant lastTrainedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserMuscleRankStat() {
    }

    public UserMuscleRankStat(AppUser user, String muscleCode) {
        this.user = user;
        this.muscleCode = muscleCode;
    }

    @PrePersist
    @PreUpdate
    void onSave() {
        updatedAt = Instant.now();
    }

    public String getMuscleCode() {
        return muscleCode;
    }

    public double getRankPoints() {
        return rankPoints;
    }

    public void addRankPoints(double points) {
        rankPoints += Math.max(0d, points);
        lastTrainedAt = Instant.now();
    }
}
