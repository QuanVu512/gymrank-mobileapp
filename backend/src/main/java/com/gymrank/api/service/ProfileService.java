package com.gymrank.api.service;

import com.gymrank.api.domain.OnboardingProfileRequest;
import com.gymrank.api.domain.ProfileSummaryResponse;
import com.gymrank.api.persistence.AppUser;
import com.gymrank.api.persistence.AppUserRepository;
import com.gymrank.api.persistence.OnboardingAnswer;
import com.gymrank.api.persistence.OnboardingAnswerRepository;
import com.gymrank.api.persistence.UserProfile;
import com.gymrank.api.persistence.UserProfileRepository;
import com.gymrank.api.persistence.UserStats;
import com.gymrank.api.persistence.UserStatsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileService {

    private static final String LOCAL_DEMO_EMAIL = "local-demo@gymrank.local";

    private final AppUserRepository appUserRepository;
    private final UserProfileRepository userProfileRepository;
    private final OnboardingAnswerRepository onboardingAnswerRepository;
    private final UserStatsRepository userStatsRepository;

    public ProfileService(
            AppUserRepository appUserRepository,
            UserProfileRepository userProfileRepository,
            OnboardingAnswerRepository onboardingAnswerRepository,
            UserStatsRepository userStatsRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.userProfileRepository = userProfileRepository;
        this.onboardingAnswerRepository = onboardingAnswerRepository;
        this.userStatsRepository = userStatsRepository;
    }

    @Transactional
    public ProfileSummaryResponse saveOnboardingProfile(OnboardingProfileRequest request) {
        AppUser user = resolveUserForWrite(request.userId(), request.displayName());

        UserProfile profile = userProfileRepository.findById(user.getId())
                .orElseGet(() -> new UserProfile(user));
        profile.updateFromOnboarding(
                request.mainGoal(),
                request.experienceLevel(),
                request.trainingDaysPerWeek(),
                request.bodygraphType()
        );
        userProfileRepository.save(profile);

        UserStats stats = userStatsRepository.findById(user.getId())
                .orElseGet(() -> userStatsRepository.save(new UserStats(user)));

        onboardingAnswerRepository.saveAll(List.of(
                new OnboardingAnswer(user, "display_name", request.displayName(), request.displayName()),
                new OnboardingAnswer(user, "experience_level", request.experienceLevel(), request.experienceLevel()),
                new OnboardingAnswer(user, "main_goal", request.mainGoal(), request.mainGoal()),
                new OnboardingAnswer(user, "training_days_per_week", String.valueOf(request.trainingDaysPerWeek()), String.valueOf(request.trainingDaysPerWeek())),
                new OnboardingAnswer(user, "bodygraph_type", request.bodygraphType(), request.bodygraphType())
        ));

        return toSummary(user, profile, stats, true);
    }

    @Transactional(readOnly = true)
    public ProfileSummaryResponse getSummary(String userId) {
        Optional<UUID> publicId = parsePublicId(userId);
        if (publicId.isPresent()) {
            Optional<AppUser> user = appUserRepository.findByPublicId(publicId.get());
            if (user.isPresent()) {
                UserProfile profile = userProfileRepository.findById(user.get().getId()).orElse(null);
                UserStats stats = userStatsRepository.findById(user.get().getId()).orElse(new UserStats(user.get()));
                return toSummary(user.get(), profile, stats, profile != null);
            }
        }

        Optional<UserProfile> latestProfile = userProfileRepository.findTopByOrderByUpdatedAtDesc();
        if (latestProfile.isPresent()) {
            AppUser user = latestProfile.get().getUser();
            UserStats stats = userStatsRepository.findById(user.getId()).orElse(new UserStats(user));
            return toSummary(user, latestProfile.get(), stats, true);
        }

        return defaultSummary();
    }

    private AppUser resolveUserForWrite(String userId, String displayName) {
        Optional<UUID> publicId = parsePublicId(userId);
        if (publicId.isPresent()) {
            return appUserRepository.findByPublicId(publicId.get())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User khong ton tai."));
        }

        return appUserRepository.findByEmail(LOCAL_DEMO_EMAIL)
                .orElseGet(() -> {
                    AppUser demoUser = appUserRepository.save(new AppUser(
                            LOCAL_DEMO_EMAIL,
                            null,
                            displayName == null || displayName.isBlank() ? "Local Demo" : displayName
                    ));
                    userStatsRepository.save(new UserStats(demoUser));
                    return demoUser;
                });
    }

    private Optional<UUID> parsePublicId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(userId.trim()));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private ProfileSummaryResponse toSummary(AppUser user, UserProfile profile, UserStats stats, boolean synced) {
        return new ProfileSummaryResponse(
                user.getFullName(),
                profile == null ? "BEGINNER" : profile.getExperienceLevel(),
                profile == null ? "CONSISTENT" : profile.getFitnessGoal(),
                profile == null || profile.getTrainingDaysPerWeek() == null ? 3 : profile.getTrainingDaysPerWeek(),
                profile == null ? "SKIP" : profile.getBodygraphType(),
                stats.getLevel(),
                stats.getExpPoints(),
                stats.getCurrentStreakWeeks(),
                stats.getRankPoints(),
                synced,
                profile == null || profile.getUpdatedAt() == null ? Instant.now() : profile.getUpdatedAt()
        );
    }

    private ProfileSummaryResponse defaultSummary() {
        return new ProfileSummaryResponse(
                "Chua dong bo",
                "BEGINNER",
                "CONSISTENT",
                3,
                "SKIP",
                1,
                0,
                0,
                0,
                false,
                Instant.now()
        );
    }
}
