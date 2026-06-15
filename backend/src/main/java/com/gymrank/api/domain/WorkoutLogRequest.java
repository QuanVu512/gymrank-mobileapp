package com.gymrank.api.domain;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record WorkoutLogRequest(
        @NotBlank String exerciseCode,
        @Min(1) @Max(100) int reps,
        @Min(1) @Max(50) int sets,
        @DecimalMin("0.1") @DecimalMax("1000.0") double weightKg
) {
}
