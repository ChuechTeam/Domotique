/**
 * The main module for our amazing hospital IoT app!
 */
open module fr.domotique.web {
    requires io.vertx.launcher.application;
    requires io.vertx.web.common;
    requires io.vertx.web;
    requires io.vertx.auth.common;
    requires org.slf4j;
    requires io.netty.codec.http;
    requires com.ongres.scram.client;
    requires com.ongres.scram.common;
    requires io.vertx.sql.client.mysql;
    requires io.vertx.sql.client;
    requires com.fasterxml.jackson.core;

    // Needed only for Liquibase stuff. Depends if you want to include Liquibase or not.
    requires static liquibase.core;
    requires static com.mattbertolini.liquibase.logging.slf4j;
    requires static org.apache.commons.lang3;
    requires static org.yaml.snakeyaml;

    // Annotations
    requires static com.github.spotbugs.annotations;
}
