package fr.domotique;

import io.vertx.core.*;
import io.vertx.ext.web.*;
import io.vertx.ext.web.sstore.*;
import io.vertx.sqlclient.*;

/// The Server class holds all settings and services used during the entire lifetime of the server.
///
/// ## What it contains
/// - An [SqlClient] to access our MySQL database: [#db()]
/// - A [SessionStore] to store user sessions: [#sessionStore()]
/// - A [Config] object to get server configuration: [#config()]
/// - The [Vertx] instance to interact with the Vert.x framework: [#vertx()]
///
/// @author Dynamic
/// @param db the connection to our MySQL database
/// @param sessionStore the session store, which stores user sessions for logged-in users
///                     (note: you usually don't need to use it directly, just do [RoutingContext#session()])
/// @param config the configuration of the server
/// @param vertx the Vert.x instance
public record Server(SqlClient db, SessionStore sessionStore, Config config, Vertx vertx) {
}