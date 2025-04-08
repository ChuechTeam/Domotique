package fr.domotique.api.users;

import fr.domotique.api.userevents.*;
import lombok.*;

import java.time.*;

/// Tells which feature of the app is giving points. Also handles cooldowns to avoid abuse.
///
/// Point amounts for each event are available in [EventPoints].
/// Multiple events can use the same source, hence why they're separated.
@Getter
@AllArgsConstructor
public enum PointSource {
    /// Visiting the home page.
    VISIT_HOME_PAGE(EventPoints.VISIT_HOME_PAGE_PTS, Duration.ofHours(18)),

    /// Checking your health through the health theme page.
    CHECK_HEALTH(EventPoints.CHECK_HEALTH_PTS, Duration.ofHours(2)),

    /// Looking at the amount of energy used by some devices.
    CHECK_ENERGY(EventPoints.CHECK_ENERGY_PTS, Duration.ofHours(12)),

    /// Looking at rankings for different attribute values
    CHECK_SPORT_RANKINGS(EventPoints.CHECK_SPORT_RANKINGS_PTS*2, Duration.ofHours(1)),

    /// Looking at other users' profiles.
    CHECK_OTHER_PROFILES(EventPoints.CHECK_OTHER_PROFILES, Duration.ofHours(1)),

    /// Looking at your own profile
    CHECK_OWN_PROFILE(EventPoints.CHECK_OWN_PROFILE, Duration.ofHours(1)),

    /// Looking at a particular device.
    CHECK_DEVICE(EventPoints.CHECK_DEVICE_PTS*6, Duration.ofHours(1));

    /// The maximum number of points given before the cooldown resets.
    private final int maxPoints;
    /// The time after which the cooldown resets.
    private final Duration resetTime;
}
