package com.gymrank.api.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "onboarding_answers")
public class OnboardingAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "question_key", nullable = false)
    private String questionKey;

    @Column(name = "question_version", nullable = false)
    private int questionVersion = 1;

    @Column(name = "answer_value")
    private String answerValue;

    @Column(name = "answer_text")
    private String answerText;

    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt;

    protected OnboardingAnswer() {
    }

    public OnboardingAnswer(AppUser user, String questionKey, String answerValue, String answerText) {
        this.user = user;
        this.questionKey = questionKey;
        this.answerValue = answerValue;
        this.answerText = answerText;
    }

    @PrePersist
    void onCreate() {
        answeredAt = Instant.now();
    }
}
