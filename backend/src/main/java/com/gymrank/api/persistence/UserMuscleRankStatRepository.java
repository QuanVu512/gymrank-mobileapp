package com.gymrank.api.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserMuscleRankStatRepository extends JpaRepository<UserMuscleRankStat, Long> {

    Optional<UserMuscleRankStat> findByUserIdAndMuscleCode(Long userId, String muscleCode);

    List<UserMuscleRankStat> findByUserIdOrderByMuscleCodeAsc(Long userId);
}
