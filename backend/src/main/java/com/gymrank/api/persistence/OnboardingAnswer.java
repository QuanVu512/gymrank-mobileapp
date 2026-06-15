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
@Table(name = "onboarding_answers")
public class OnboardingAnswer {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "experience_level", nullable = false)
    private String experienceLevel;

    @Column(name = "main_goal", nullable = false)
    private String mainGoal;

    @Column(name = "training_days_per_week", nullable = false)
    private int trainingDaysPerWeek;

    @Column(name = "bodygraph_type", nullable = false)
    private String bodygraphType;

    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OnboardingAnswer() {
    }

    public OnboardingAnswer(AppUser user) {
        this.user = user;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        answeredAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void updateFromOnboarding(
            String displayName,
            String experienceLevel,
            String mainGoal,
            int trainingDaysPerWeek,
            String bodygraphType
    ) {
        this.displayName = displayName;
        this.experienceLevel = experienceLevel;
        this.mainGoal = mainGoal;
        this.trainingDaysPerWeek = trainingDaysPerWeek;
        this.bodygraphType = bodygraphType;
    }
}
