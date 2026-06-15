package com.gymrank.api.controller;

import com.gymrank.api.domain.AuthRequest;
import com.gymrank.api.domain.AuthResponse;
import com.gymrank.api.domain.Exercise;
import com.gymrank.api.domain.OnboardingProfileRequest;
import com.gymrank.api.domain.ProfileSummaryResponse;
import com.gymrank.api.domain.WorkoutScoreRequest;
import com.gymrank.api.domain.WorkoutScoreResponse;
import com.gymrank.api.persistence.AppUser;
import com.gymrank.api.security.AuthTokenInterceptor;
import com.gymrank.api.service.AuthService;
import com.gymrank.api.service.ProfileService;
import com.gymrank.api.service.ScoringService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class GymRankController {

    private final ScoringService scoringService;
    private final ProfileService profileService;
    private final AuthService authService;

    public GymRankController(ScoringService scoringService, ProfileService profileService, AuthService authService) {
        this.scoringService = scoringService;
        this.profileService = profileService;
        this.authService = authService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "service", "gymrank-api",
                "time", Instant.now().toString()
        );
    }

    @GetMapping("/exercises")
    public List<Exercise> exercises() {
        return List.of(
                new Exercise("push_up", "Hít đất", "Chest", List.of("Triceps", "Shoulders"), true),
                new Exercise("bodyweight_squat", "Squat không tạ", "Quads", List.of("Glutes", "Core"), true),
                new Exercise("plank", "Plank", "Core", List.of("Shoulders"), true),
                new Exercise("barbell_curl", "Cuốn tay đòn", "Biceps", List.of("Forearms"), false)
        );
    }

    @PostMapping("/score/preview")
    public WorkoutScoreResponse previewScore(@Valid @RequestBody WorkoutScoreRequest request) {
        return scoringService.preview(request);
    }

    @PostMapping("/auth/register")
    public AuthResponse register(@Valid @RequestBody AuthRequest request) {
        return authService.register(request);
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/onboarding/profile")
    public ProfileSummaryResponse saveOnboardingProfile(@Valid @RequestBody OnboardingProfileRequest request, HttpServletRequest servletRequest) {
        return profileService.saveOnboardingProfile(authenticatedUser(servletRequest), request);
    }

    @GetMapping("/me/summary")
    public ProfileSummaryResponse profileSummary(HttpServletRequest servletRequest) {
        return profileService.getSummary(authenticatedUser(servletRequest));
    }

    private AppUser authenticatedUser(HttpServletRequest request) {
        return (AppUser) request.getAttribute(AuthTokenInterceptor.AUTHENTICATED_USER_ATTRIBUTE);
    }
}
