package fr.domotique;

import io.vertx.core.*;
import io.vertx.launcher.application.*;

/// The main class that just deploys the [MainVerticle]. No need for extraneous console arguments!
///
/// @author Dynamic
/// @see VertxApplication
/// @see MainVerticle
public class Launcher extends VertxApplication {
    public Launcher(String[] args) {
        super(args, new VertxApplicationHooks() {
            @Override
            public void beforeDeployingVerticle(HookContext context) {
                // Make sure we launch our MainVerticle with a virtual thread
                // to greatly simplify initialization.
                context.deploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD);
            }
        });
    }

    public static void main(String[] args) {
        var totalArgs = new String[args.length + 1];

        // Force the first argument to be the name of MainVerticle
        totalArgs[0] = MainVerticle.class.getName();
        System.arraycopy(args, 0, totalArgs, 1, args.length);

        var launcher = new Launcher(totalArgs);
        launcher.launch();
    }
}
