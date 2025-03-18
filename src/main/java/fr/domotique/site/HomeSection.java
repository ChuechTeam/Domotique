package fr.domotique.site;

import fr.domotique.*;
import io.vertx.core.*;
import io.vertx.core.buffer.*;
import io.vertx.ext.web.*;

public class HomeSection extends Section {
    public HomeSection(Server server) {
        super(server);
    }

    @Override
    public void register(Router router) {
        router.get("/").respond(this::index);
    }

    Future<Buffer> index(RoutingContext ctx) {
        return view(ctx, "hello.jte",
                viewArg("test", 123));
    }
}
