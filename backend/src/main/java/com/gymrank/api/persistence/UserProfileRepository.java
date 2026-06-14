package com.gymrank.api.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserPublicId(UUID publicId);

    Optional<UserProfile> findTopByOrderByUpdatedAtDesc();
}
