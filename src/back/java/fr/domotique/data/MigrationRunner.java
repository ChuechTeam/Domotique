package fr.domotique.data;

import fr.domotique.*;
import liquibase.command.*;
import liquibase.exception.*;
import liquibase.exception.CommandExecutionException;
import liquibase.integration.commandline.*;
import liquibase.resource.*;

/// Updates the database by giving easy access to Liquibase.
///
/// Allows you to:
/// - run Liquibase with predefined arguments using Domotique's [Config]
/// - programmatically update the database (by using [#update(Config)] )
///
/// To try out the first option, run the gradle `updateDatabase` command.
///
/// See [Liquibase docs](https://docs.liquibase.com/commands/command-list.html) for more info.
public final class MigrationRunner {

    /// Runs the Liquibase CLI.
    ///
    /// To use, run the gradle `updateDatabase` command: `gradle updateDatabase`
    ///
    /// You can also put custom arguments, like `gradle updateDatabase --args="downgrade"`
    public static void main(String[] args) throws Exception {
        var config = Config.load();
        var builtinArgs = new String[]{
                "--changeLogFile=changelog.sql",
                "--url=jdbc:" + config.databaseUri()
        };

        var totalArgs = new String[builtinArgs.length + args.length];
        System.arraycopy(builtinArgs, 0, totalArgs, 0, builtinArgs.length);
        System.arraycopy(args, 0, totalArgs, builtinArgs.length, args.length);

        LiquibaseCommandLine.main(totalArgs);
    }

    /**
     * Updates the database to the latest version.
     *
     * @throws CommandExecutionException if something went wrong
     */
    public static void update(Config config) throws CommandExecutionException {
        new CommandScope("update")
                .addArgumentValue("changeLogFile", "changelog.sql")
                .addArgumentValue("resourceAccessor", new ClassLoaderResourceAccessor())
                .addArgumentValue("url", "jdbc:" + config.databaseUri())
                .execute();
    }
}
