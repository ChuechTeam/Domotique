package fr.domotique;

import fr.domotique.data.*;
import fr.domotique.email.*;
import io.vertx.core.*;
import io.vertx.ext.web.common.template.*;
import io.vertx.ext.web.sstore.*;
import io.vertx.sqlclient.*;

/// The Server class holds all settings and services used during the entire lifetime of the server.
///
/// ## What it contains
/// - An [SqlClient] to access our MySQL database: [#sql()]
/// - The [Database] object, which contains all tables: [#db()]
/// - A [SessionStore] to store user sessions: [#sessionStore()]
/// - A [TemplateEngine] to render HTML JTE templates: [#templateEngine()]
/// - An [EmailSender] to send emails to users: [#email()]
/// - A [Config] object to get server configuration: [#config()]
/// - The [Vertx] instance to interact with the Vert.x framework: [#vertx()]
///
/// @author Dynamic
/// @param sql the connection to our MySQL database
/// @param db the database object, containing all tables
/// @param sessionStore the session store, which stores user sessions for logged-in users
///                     (note: you usually don't need to use it directly, just use `RoutingContext#session()`)
/// @param templateEngine the template engine to render HTML templates using JTE
/// @param email the email sender to send emails to users
/// @param config the configuration of the server
/// @param vertx the Vert.x instance
public record Server(SqlClient sql,
                     Database db,
                     SessionStore sessionStore,
                     TemplateEngine templateEngine,
                     EmailSender email,
                     Config config,
                     Vertx vertx) {
}