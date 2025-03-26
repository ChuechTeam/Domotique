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

        r.post("/home-page-visit")
            .respond(ctx -> {
                var id = Authenticator.get(ctx).getUserId();
                UserOperations.enqueuePointsIncrease(ctx.vertx(), id, PointSource.VISIT_HOME_PAGE, EventPoints.VISIT_HOME_PAGE_PTS);
                return Future.succeededFuture();
            })
            .putMetadata(RouteDoc.KEY, new RouteDoc("reportHomePageVisit")
                .summary("Report Event: Home page visited")
                .response(201, "The visit was reported"));
    }
}
