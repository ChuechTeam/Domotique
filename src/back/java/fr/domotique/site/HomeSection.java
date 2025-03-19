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
        // Link all routes to our functions
//        router.get("/").respond(this::index);
    }

    // GET /
    // ------ The home page of our website
    Future<Buffer> index(RoutingContext ctx) {
        // Return the "hello.jte" with a value test = 123
        return view(ctx, "hello.jte",
                viewArg("test", 123));
    }
}
