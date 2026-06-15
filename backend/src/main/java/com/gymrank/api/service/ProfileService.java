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

@Service
public class ProfileService {

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
    public ProfileSummaryResponse saveOnboardingProfile(AppUser authenticatedUser, OnboardingProfileRequest request) {
        AppUser user = requireExistingUser(authenticatedUser);

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

        OnboardingAnswer onboardingAnswer = onboardingAnswerRepository.findById(user.getId())
                .orElseGet(() -> new OnboardingAnswer(user));
        onboardingAnswer.updateFromOnboarding(
                request.displayName(),
                request.experienceLevel(),
                request.mainGoal(),
                request.trainingDaysPerWeek(),
                request.bodygraphType()
        );
        onboardingAnswerRepository.save(onboardingAnswer);

        return toSummary(user, profile, stats, true);
    }

    @Transactional(readOnly = true)
    public ProfileSummaryResponse getSummary(AppUser authenticatedUser) {
        AppUser user = requireExistingUser(authenticatedUser);
        UserProfile profile = userProfileRepository.findById(user.getId()).orElse(null);
        UserStats stats = userStatsRepository.findById(user.getId()).orElse(new UserStats(user));
        return toSummary(user, profile, stats, profile != null);
    }

    private AppUser requireExistingUser(AppUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "API này cần đăng nhập.");
        }
        return appUserRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Người dùng không tồn tại."));
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
}
