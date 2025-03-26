package fr.domotique.api.users;

import fr.domotique.*;
import fr.domotique.base.*;
import fr.domotique.data.*;
import io.vertx.core.*;
import io.vertx.core.eventbus.*;
import lombok.*;
import org.slf4j.*;

import java.time.*;
import java.util.*;

/// A background verticle that just adds points to users.
///
/// Use [UserOperations#enqueuePointsIncrease(io.vertx.core.Vertx, int, int)] to add points to a user.
///
/// Must be deployed once only.
public class PointsVerticle extends VerticleBase {
    private static final Logger log = LoggerFactory.getLogger(PointsVerticle.class);

    /// The list of pending messages. Is cleared after each processing session.
    private final List<AddPointsMessage> pendingMessages = new ArrayList<>();

    /// The list of applied cooldowns for each user.
    private final Map<Integer, EnumMap<PointSource, CooldownData>> cooldowns = new HashMap<>();

    private MessageConsumer<AddPointsMessage> consumer;
    private long timer;

    private final Server server;

    public PointsVerticle(Server server) {
        this.server = server;
    }

    @Override
    public Future<?> start() throws Exception {
        vertx.eventBus().registerDefaultCodec(AddPointsMessage.class, new LocalCodec<>());

        consumer = vertx.eventBus().consumer(AddPointsMessage.CHANNEL, msg -> pendingMessages.add(msg.body()));
        timer = vertx.setPeriodic(1000, this::processAllMessages);

        return Future.succeededFuture();
    }

    @Override
    public Future<?> stop() {
        vertx.cancelTimer(timer);
        return consumer.unregister();
    }

    private void processAllMessages(long timerId) {
        if (pendingMessages.isEmpty()) {
            return;
        }

        long nanos = System.nanoTime();

        // Calculate the total amount of points to add: user id -> sum of points to add
        // And also apply cooldowns.
        var now = Instant.now();
        var pointMap = new HashMap<Integer, Integer>();
        for (AddPointsMessage msg : pendingMessages) {
            // Find the cooldown and reset it if possible.
            CooldownData cd = findCooldown(msg.userId(), msg.source());
            cd.resetIfNecessary(now, msg.source());

            // Calculate the number of points based on the amount given during the cooldown.
            int pts = msg.points();
            pts = cd.accept(pts, msg.source());

            // Add the points to the points map, only if they are positive of course.
            if (pts > 0) {
                pointMap.merge(msg.userId(), pts, Integer::sum);
            }
        }

        if (log.isDebugEnabled()) {
            long elapsed = System.nanoTime() - nanos;
            log.debug("Computed point map for {} point increases in {} µs", pendingMessages.size(), elapsed / 1000);
            log.debug("Points to add:{}", pointMap.entrySet().stream()
                .map(e -> String.format("\nUser %4s: %4s points", e.getKey(), e.getValue()))
                .reduce(String::concat)
                .orElse(" None"));
        }

        // Reset the list of pending messages
        pendingMessages.clear();

        // Query the database for the current points of all users
        server.db().users().getManyPointInfo(pointMap.keySet())
            .compose(pts -> {
                // Look at all changed users; unknown users are silently ignored for now.
                var updates = new ArrayList<UserTable.PointInfo>();
                for (UserTable.PointInfo pt : pts) {
                    // See the points we should add.
                    int pointsAdded = pointMap.getOrDefault(pt.id(), 0);

                    // If we should add points, calculate the new level and points.
                    if (pointsAdded > 0) {
                        int oldPoints = pt.points();
                        int newPoints = oldPoints + pointsAdded;
                        Level newLevel = UserOperations.newLevelAfterAddingPoints(pt.level(), oldPoints, newPoints);

                        // Add an update to the list.
                        updates.add(new UserTable.PointInfo(pt.id(), newPoints, newLevel));
                    }
                }

                // Push these updates to the database.
                return server.db().users().updateManyPointInfo(updates);
            })
            .andThen(x -> {
                if (x.succeeded()) {
                    long elapsed = System.nanoTime() - nanos;
                    log.info("Processed {} point increases in {} µs", pointMap.size(), elapsed / 1000);
                } else {
                    log.error("Failed to process points add messages", x.cause());
                    // todo: retry?
                }
            });
    }

    private CooldownData findCooldown(int userId, PointSource source) {
        return cooldowns
            .computeIfAbsent(userId, _ -> new EnumMap<>(PointSource.class))
            .computeIfAbsent(source, src -> new CooldownData(src.getResetTime()));
    }

    /// Cooldown information for a (user, source) pair.
    @AllArgsConstructor
    static class CooldownData {
        CooldownData(Duration cooldown) {
            this(0, Instant.now().plus(cooldown));
        }

        /// Points given during the cooldown period.
        int pointsGiven;
        /// Time at which [#pointsGiven] should be reset.
        Instant nextReset;

        void resetIfNecessary(Instant now, PointSource src) {
            if (nextReset.isBefore(now)) {
                pointsGiven = 0;
                nextReset = now.plus(src.getResetTime());
            }
        }

        int accept(int points, PointSource src) {
            int remaining = Math.max(0, src.getMaxPoints() - pointsGiven);
            int toAccept = Math.min(points, remaining);
            pointsGiven += toAccept;
            return toAccept;
        }
    }
}
