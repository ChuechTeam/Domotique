package fr.domotique.api.health;

import fr.domotique.*;
import fr.domotique.base.*;
import fr.domotique.base.apidocs.*;
import fr.domotique.data.*;
import io.vertx.ext.web.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static fr.domotique.api.health.HealthStatus.healthyWhen;

public class HealthSection extends Section {
    public HealthSection(Server server) {
        super(server);
    }

    @Override
    public void register(Router router) {
        var sr = newSubRouter(router, "/api/health*")
            .putMetadata(RouteDoc.KEY, new RouteDoc().tag("Health"));

        sr.route().handler(routingContext -> {
            Authenticator.get(routingContext).requireAuth(Level.BEGINNER);
            routingContext.next();
        });
        sr.get("/my-health").respond(vt(this::myHealth)).putMetadata(RouteDoc.KEY, MY_HEALTH_DOC);
    }

    static final RouteDoc MY_HEALTH_DOC = new RouteDoc("getMyHealth")
        .summary("Get the health status of the current user")
        .description("Returns the health status of the current user, including various health attributes.")
        .response(200, MyHealthOutput.class, "Here's your health status!")
        .response(401, "Unauthorized");

    MyHealthOutput myHealth(RoutingContext ctx) {
        var userId = Authenticator.get(ctx).getUserId();

        var entries = server.db().devices().getAllAttributesOfOwner(userId).await();

        // Contains the sum of all attributes
        var attributeValues = new EnumMap<AttributeType, Object>(AttributeType.class);
        var attributeOccurrences = new EnumMap<AttributeType, Integer>(AttributeType.class);
        var attributeSources = new EnumMap<AttributeType, ArrayList<Integer>>(AttributeType.class);
        for (var entry : entries) {
            for (var kv : entry.attributes().entrySet()) {
                AttributeType attr = kv.getKey();
                Object value = kv.getValue();

                if (attr.getContent() == AttributeType.Content.NUMBER) {
                    attributeValues.merge(attr, value, (a, b) -> toDouble(a) + toDouble(b));
                    attributeOccurrences.merge(attr, 1, Integer::sum);
                    attributeSources.computeIfAbsent(attr, _ -> new ArrayList<>()).add(entry.deviceId());
                } else {
                    attributeValues.put(attr, value);
                }
            }
        }

        // Average out values inside attributeValues.
        for (var occ : attributeOccurrences.entrySet()) {
            AttributeType attr = occ.getKey();
            int count = occ.getValue();

            if (attr.getContent() == AttributeType.Content.NUMBER && count > 1) {
                double current = (Double) attributeValues.get(attr);
                double average = current / count;
                attributeValues.put(attr, average);
            }
        }

        var healthValues = new EnumMap<AttributeType, HealthValue>(AttributeType.class);
        for (var entry : attributeValues.entrySet()) {
            AttributeType attr = entry.getKey();
            Object value = entry.getValue();

            HealthValue healthValue = evaluate(attr, attributeValues, value, attributeSources.get(attr));
            if (healthValue != null) {
                healthValues.put(attr, healthValue);
            }
        }

        return new MyHealthOutput(healthValues);
    }

    record MyHealthOutput(EnumMap<AttributeType, HealthValue> values) {}

    record HealthValue(Object value, @Nullable AttributeRange range, HealthStatus status, List<Integer> deviceIds) {}

    private @Nullable HealthValue evaluate(AttributeType attr, EnumMap<AttributeType, Object> attributes, Object value, List<Integer> devices) {
        AttributeRange range = calcHealthyRange(attr, attributes);
        if (range == null) {
            return null;
        } else if (range.equals(AttributeRange.INFINITE)) {
            return new HealthValue(value, null, HealthStatus.MISSING_DATA, devices);
        } else {
            double val = toDouble(value);
            return new HealthValue(value, range, val >= range.min() && val <= range.max() ? HealthStatus.HEALTHY : HealthStatus.WARNING, devices);
        }
    }

    private @Nullable AttributeRange calcHealthyRange(AttributeType attr, EnumMap<AttributeType, Object> attributes) {
        return switch (attr) {
            case AttributeType.BODY_WEIGHT -> {
                Object height = attributes.get(AttributeType.BODY_HEIGHT);
                if (height == null) {
                    yield AttributeRange.INFINITE;
                }
                // Calculate BMI-based weight range
                double heightInMeters = toDouble(height) / 100.0; // Convert cm to meters
                double minWeight = 18.5 * heightInMeters * heightInMeters; // Minimum healthy weight (BMI 18.5)
                double maxWeight = 25.0 * heightInMeters * heightInMeters; // Maximum healthy weight (BMI 25)
                yield new AttributeRange(minWeight, maxWeight);
            }
            case AttributeType.HEART_RATE -> new AttributeRange(60, 100);
            case AttributeType.BLOOD_PRESSURE -> new AttributeRange(90, 120);
            case AttributeType.BLOOD_OXYGEN -> new AttributeRange(95, 100);
            case AttributeType.BLOOD_GLUCOSE -> new AttributeRange(70, 140);
            case AttributeType.BODY_TEMPERATURE -> new AttributeRange(36.1, 37.2);
            case AttributeType.RESPIRATORY_RATE -> new AttributeRange(12, 20);
            case AttributeType.FAT_PERCENTAGE -> new AttributeRange(10, 31);
            case AttributeType.MAX_VO2 -> new AttributeRange(15, Double.MAX_VALUE);
            default -> null; // For ignored attributes
        };
    }

    // Is inclusive
    record AttributeRange(double min, double max) {
        public static AttributeRange INFINITE = new AttributeRange(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    private static double toDouble(Object v) {
        if (v instanceof Number) {
            return ((Number) v).doubleValue();
        } else {
            throw new IllegalArgumentException("Invalid value type: " + v.getClass());
        }
    }
}
