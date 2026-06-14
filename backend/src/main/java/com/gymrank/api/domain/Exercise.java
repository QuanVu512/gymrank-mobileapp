package com.gymrank.api.domain;

import java.util.List;

public record Exercise(
        String code,
        String nameVi,
        String primaryMuscle,
        List<String> secondaryMuscles,
        boolean beginnerSafe
) {
}
