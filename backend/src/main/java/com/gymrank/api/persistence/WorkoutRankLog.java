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
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "workout_rank_logs",
        indexes = @Index(name = "idx_workout_rank_logs_user_created", columnList = "user_id, created_at")
)
public class WorkoutRankLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "exercise_code", nullable = false, length = 80)
    private String exerciseCode;

    @Column(nullable = false)
    private int reps;

    @Column(nullable = false)
    private int sets;

    @Column(name = "weight_kg", nullable = false)
    private double weightKg;

    @Column(name = "exp_gained", nullable = false)
    private int expGained;

    @Column(name = "rank_gained", nullable = false)
    private double rankGained;

    @Column(name = "cheat_like", nullable = false)
    private boolean cheatLike;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected WorkoutRankLog() {
    }

    public WorkoutRankLog(AppUser user, String exerciseCode, int reps, int sets, double weightKg, int expGained, double rankGained, boolean cheatLike) {
        this.user = user;
        this.exerciseCode = exerciseCode;
        this.reps = reps;
        this.sets = sets;
        this.weightKg = weightKg;
        this.expGained = expGained;
        this.rankGained = rankGained;
        this.cheatLike = cheatLike;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
