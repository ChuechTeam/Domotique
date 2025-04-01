package fr.domotique.api.users;

import io.vertx.core.eventbus.*;

/// A message sent to the Vert.x [EventBus] to give points to a user.
public record AddPointsMessage(int userId, PointSource source, int points) {
    public AddPointsMessage {
        if (points < 0) {
            throw new IllegalArgumentException("Points cannot be negative.");
        }
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID.");
        }
    }

    public static final String CHANNEL = "addUserPoints";
}
