package fr.domotique.api.energy;

import fr.domotique.*;
import fr.domotique.base.*;
import fr.domotique.base.apidocs.*;
import fr.domotique.data.*;
import io.vertx.core.*;
import io.vertx.ext.web.*;

import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;

public class EnergySection extends Section {
    public EnergySection(Server server) {
        super(server);
    }

    @Override
    public void register(Router router) {
        var sr = newSubRouter(router, "/api/energy*")
            .putMetadata(RouteDoc.KEY, new RouteDoc().tag("Energy"));

        sr.route().handler(ctx -> {
            Authenticator.get(ctx).requireAuth(Level.BEGINNER);
            ctx.next();
        });

        sr.get("/total-consumption").respond(ctx -> {
            // Parse dates in ISO format
            var start = ctx.request().getParam("start");
            var end = ctx.request().getParam("end");
            if (start == null || end == null) {
                return Future.failedFuture(new RequestException("Missing start or end parameter", 400));
            }

            try {
                var startTime = Instant.parse(start);
                var endTime = Instant.parse(end);

                return server.db().powerLogs().queryTotalConsumption(startTime, endTime)
                    .map(x -> {
                        double totalConsumption = x.stream().mapToDouble(PowerLogTable.Consumption::consumption).sum();
                        return new ConsumptionOutput(totalConsumption, x);
                    });
            } catch (DateTimeParseException e) {
                return Future.failedFuture(new RequestException("Invalid date format", 400));
            }
        }).putMetadata(RouteDoc.KEY, new RouteDoc("getTotalConsumption")
            .summary("Get the total consumption")
            .description("Get the total consumption with the given time range, in Wh.")
            .queryParam("start", String.class, ParamDoc.Format.DATE_TIME, "Start time in ISO format")
            .queryParam("end", String.class, ParamDoc.Format.DATE_TIME, "End time in ISO format")
            .response(200, ConsumptionOutput.class, "The result"));
    }

    // todo: move consumption in api folder
    record ConsumptionOutput(double totalConsumption, List<PowerLogTable.Consumption> perDevice) {}
}
