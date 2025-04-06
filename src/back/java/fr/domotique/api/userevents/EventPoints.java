package fr.domotique.api.userevents;

/// Number of points assigned to each event.
public final class EventPoints {
    /// Points per visit to the home page.
    public static final int VISIT_HOME_PAGE_PTS = 80;

    /// Points per health check.
    public static final int CHECK_HEALTH_PTS = 40;

    /// Points per energy check.
    public static final int CHECK_ENERGY_PTS = 40;

    /// Points per sport rankings check.
    public static final int CHECK_SPORT_RANKINGS_PTS = 20;

    /// Points for checking other user profiles.
    public static final int CHECK_OTHER_PROFILES = 25;

    /// Points for checking your own profile.
    public static final int CHECK_OWN_PROFILE = 10;

    /// Points per device check.
    public static final int CHECK_DEVICE_PTS = 15;

    // Private constructor to prevent instantiation
    private EventPoints() {
    }
}
