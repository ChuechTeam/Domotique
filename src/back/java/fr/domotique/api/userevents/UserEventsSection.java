package fr.domotique.api.userevents;

import fr.domotique.*;
import fr.domotique.api.users.*;
import fr.domotique.base.*;
import fr.domotique.base.apidocs.*;
import fr.domotique.data.*;
import io.vertx.core.*;
import io.vertx.ext.web.*;

public class UserEventsSection extends Section {
    public UserEventsSection(Server server) {
        super(server);
    }

    @Override
    public void register(Router router) {
        var r = newSubRouter(router, "/api/user-events*")
            .putMetadata(RouteDoc.KEY, new RouteDoc().tag("User Events"));

        // We need to be authenticated, with email confirmed
        r.route().handler(ctx -> {
            Authenticator.get(ctx).requireAuth(Level.BEGINNER);
            ctx.next();
        });

        registerRoute(r, "/home-page-visit", PointSource.VISIT_HOME_PAGE, EventPoints.VISIT_HOME_PAGE_PTS, "reportHomePageVisit");
        registerRoute(r, "/check-health", PointSource.CHECK_HEALTH, EventPoints.CHECK_HEALTH_PTS, "reportHealthCheck");
        registerRoute(r, "/check-energy", PointSource.CHECK_ENERGY, EventPoints.CHECK_ENERGY_PTS, "reportEnergyCheck");
        registerRoute(r, "/check-sport-rankings", PointSource.CHECK_SPORT_RANKINGS, EventPoints.CHECK_SPORT_RANKINGS_PTS, "reportSportRankingsCheck");
        registerRoute(r, "/check-other-profiles", PointSource.CHECK_OTHER_PROFILES, EventPoints.CHECK_OTHER_PROFILES, "reportOtherProfilesCheck");
        registerRoute(r, "/check-own-profile", PointSource.CHECK_OWN_PROFILE, EventPoints.CHECK_OWN_PROFILE, "reportOwnProfileCheck");
        registerRoute(r, "/check-device", PointSource.CHECK_DEVICE, EventPoints.CHECK_DEVICE_PTS, "reportDeviceCheck");
    }

    private void registerRoute(Router r, String url, PointSource ps, int points,  String opName) {
        r.post(url)
            .respond(ctx -> {
                var id = Authenticator.get(ctx).getUserId();
                UserOperations.enqueuePointsIncrease(ctx.vertx(), id, ps, points);
                // todo: don't put this header when cooldown is active
                ctx.response().putHeader("Domotique-Refresh-Points", "true");
                return Future.succeededFuture();
            })
            .putMetadata(RouteDoc.KEY, new RouteDoc(opName)
                .summary("Report Event: " + ps)
                .response(201, "The event was reported"));
    }
}
