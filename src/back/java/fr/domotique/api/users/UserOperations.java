package fr.domotique.api.users;

import fr.domotique.data.*;
import io.vertx.core.*;

/// Contains common logic, operations and functions for users.
public final class UserOperations {
    private UserOperations() {
    }

    /// Points needed to become an intermediate user
    public static final int INTERMEDIATE_POINTS = 200;
    /// Points needed to become an advanced user
    public static final int ADVANCED_POINTS = 500;
    /// Points needed to become an expert user
    public static final int EXPERT_POINTS = 1000;

    /// Returns the new [Level] of a user after their points went from `oldPoints` to `newPoints`.
    public static Level newLevelAfterAddingPoints(Level current, int oldPoints, int newPoints) {
        if (oldPoints < 0 || newPoints < 0) {
            return current;
        }

        if (oldPoints < INTERMEDIATE_POINTS && newPoints >= INTERMEDIATE_POINTS && current.compareTo(Level.INTERMEDIATE) < 0) {
            return Level.INTERMEDIATE;
        } else if (oldPoints < ADVANCED_POINTS && newPoints >= ADVANCED_POINTS && current.compareTo(Level.ADVANCED) < 0) {
            return Level.ADVANCED;
        } else if (oldPoints < EXPERT_POINTS && newPoints >= EXPERT_POINTS && current.compareTo(Level.EXPERT) < 0) {
            return Level.EXPERT;
        } else {
            return current;
        }
    }

    /// Adds points for a user after a short while.
    ///
    /// The points will be added in the background, thanks to the [PointsVerticle].
    public static void enqueuePointsIncrease(Vertx vertx, int userId, PointSource source, int points) {
        // TODO: Provide a way to know if the points of the cooldown period have been depleted before sending points,
        //       so we don't send useless messages
        vertx.eventBus().publish(AddPointsMessage.CHANNEL, new AddPointsMessage(userId, source, points));
    }
}
