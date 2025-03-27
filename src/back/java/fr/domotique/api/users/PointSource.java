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
    VISIT_HOME_PAGE(EventPoints.VISIT_HOME_PAGE_PTS, Duration.ofHours(1));

    /// The maximum number of points given before the cooldown resets.
    private final int maxPoints;
    /// The time after which the cooldown resets.
    private final Duration resetTime;
}
