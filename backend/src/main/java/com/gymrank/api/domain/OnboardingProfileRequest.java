package com.gymrank.api.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record OnboardingProfileRequest(
        String userId,
        @NotBlank String displayName,
        @NotBlank String experienceLevel,
        @NotBlank String mainGoal,
        @Min(1) @Max(7) int trainingDaysPerWeek,
        @NotBlank String bodygraphType
) {
}
