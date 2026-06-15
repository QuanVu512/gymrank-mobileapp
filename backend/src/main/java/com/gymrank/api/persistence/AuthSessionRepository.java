package com.gymrank.api.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {

    @Query("""
            select session
            from AuthSession session
            join fetch session.user
            where session.tokenHash = :tokenHash
              and session.revokedAt is null
            """)
    Optional<AuthSession> findActiveCandidateByTokenHash(@Param("tokenHash") String tokenHash);
}
