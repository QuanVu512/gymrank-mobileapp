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
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private AppUser user;

    private String gender;

    @Column(name = "fitness_goal")
    private String fitnessGoal;

    @Column(name = "experience_level")
    private String experienceLevel;

    @Column(name = "training_days_per_week")
    private Integer trainingDaysPerWeek;

    @Column(name = "bodygraph_type")
    private String bodygraphType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserProfile() {
    }

    public UserProfile(AppUser user) {
        this.user = user;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public AppUser getUser() {
        return user;
    }

    public String getFitnessGoal() {
        return fitnessGoal;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public Integer getTrainingDaysPerWeek() {
        return trainingDaysPerWeek;
    }

    public String getBodygraphType() {
        return bodygraphType;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateFromOnboarding(String fitnessGoal, String experienceLevel, int trainingDaysPerWeek, String bodygraphType) {
        this.fitnessGoal = fitnessGoal;
        this.experienceLevel = experienceLevel;
        this.trainingDaysPerWeek = trainingDaysPerWeek;
        this.bodygraphType = bodygraphType;
        this.gender = bodygraphType;
    }
}
