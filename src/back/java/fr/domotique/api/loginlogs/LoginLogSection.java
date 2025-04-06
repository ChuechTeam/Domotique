package fr.domotique.api.loginlogs;

import fr.domotique.*;
import fr.domotique.api.users.*;
import fr.domotique.base.*;
import fr.domotique.base.apidocs.*;
import fr.domotique.data.*;
import io.vertx.ext.web.*;

import java.util.*;
import java.util.stream.*;

public class LoginLogSection extends Section {
    public LoginLogSection(Server server) {
        super(server);
    }

    @Override
    public void register(Router router) {
        var subRouter = newSubRouter(router, "/api/login-logs*")
            .putMetadata(RouteDoc.KEY, new RouteDoc()
                .tag("Login Logs")
                .response(401, ErrorResponse.class, "You aren't logged in.")
                .response(403, ErrorResponse.class, "You don't have enough permissions."));

        // All routes need an EXPERT level to be used.
        subRouter.route().handler(ctx -> {
            Authenticator.get(ctx).requireAuth(Level.EXPERT);
            ctx.next();
        });

        // GET /api/login-logs?userId
        subRouter.get()
            .respond(ctx ->
                server.db().loginLogs().getAllSorted(readIntOrNull(ctx.queryParams().get("userId")))
                    .compose(logs -> {
                        var userIds = logs.stream().map(LoginLog::getUserId).collect(Collectors.toSet());
                        return server.db().users().getAllProfiles(userIds)
                            .map(users -> new LoginLogsResponse(logs, users));
                    }))
            .putMetadata(RouteDoc.KEY, new RouteDoc("getLoginLogs")
                .summary("Get all login logs")
                .description("Returns a list of all login logs in the database")
                .optionalQueryParam("userId", int.class, "The user id to filter the logs by")
                .response(200, LoginLogsResponse.class, "The list of login logs"));
    }

    record LoginLogsResponse(List<LoginLog> logs, List<UserProfile> users) {}
}
