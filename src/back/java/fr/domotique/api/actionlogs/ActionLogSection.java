package fr.domotique.api.actionlogs;

import fr.domotique.*;
import fr.domotique.base.*;
import fr.domotique.base.apidocs.*;
import fr.domotique.data.*;
import io.vertx.ext.web.*;

import java.util.*;

public class ActionLogSection extends Section {
    public ActionLogSection(Server server) {
        super(server);
    }

    @Override
    public void register(Router router) {
        var subRouter = newSubRouter(router, "/api/action-logs*")
            .putMetadata(RouteDoc.KEY, new RouteDoc()
                .tag("Action Logs")
                .response(401, ErrorResponse.class, "You aren't logged in.")
                .response(403, ErrorResponse.class, "You don't have enough permissions."));

        // All routes need an EXPERT level to be used.
        subRouter.route().handler(ctx -> {
            Authenticator.get(ctx).requireAuth(Level.EXPERT);
            ctx.next();
        });

        // GET /api/action-logs?userId&operation&target&desc
        subRouter.get()
            .respond(ctx -> {
                var query = new ActionLogTable.Query(
                    readIntOrNull(ctx.queryParams().get("userId")),
                    readEnumOrNull(ActionLogOperation.class, ctx.queryParams().get("operation")),
                    readEnumOrNull(ActionLogTarget.class, ctx.queryParams().get("target")),
                    !ctx.queryParams().contains("desc") || Boolean.parseBoolean(ctx.queryParams().get("desc"))
                );
                return server.db().actionLogs().query(query)
                    .map(ActionLogsResponse::new);
            })
            .putMetadata(RouteDoc.KEY, new RouteDoc("getActionLogs")
                .summary("Get filtered action logs")
                .description("Returns a list of action logs filtered by the provided parameters")
                .optionalQueryParam("userId", int.class, "Filter logs by the user who performed the action")
                .optionalQueryParam("operation", ActionLogOperation.class, "Filter logs by operation type")
                .optionalQueryParam("target", ActionLogTarget.class, "Filter logs by target type")
                .optionalQueryParam("desc", boolean.class, "Sort in descending order by time if true, ascending if false. Default is false.")
                .response(200, ActionLogsResponse.class, "The filtered list of action logs"));
    }

    private <T extends Enum<T>> T readEnumOrNull(Class<T> enumClass, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    record ActionLogsResponse(List<CompleteActionLog> logs) {}
}
