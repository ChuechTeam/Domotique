/**
 * The main module for our amazing EHPAD IoT app!
 */
open module fr.domotique.web {
    requires io.vertx.launcher.application;
    requires io.vertx.web;
    requires io.vertx.auth.common;
    requires org.slf4j;
    requires io.netty.codec.http;
    requires io.vertx.sql.client.mysql;
    requires io.vertx.sql.client;
    requires com.fasterxml.jackson.core;

    requires io.vertx.httpproxy;
    requires io.vertx.web.proxy;

    requires io.swagger.v3.oas.models;

    // Templating
    requires io.vertx.web.template.jte;
    requires io.vertx.web.common;
    requires gg.jte;
    requires gg.jte.runtime;

    // Needed only for Liquibase stuff. Depends if you want to include Liquibase or not.
    requires static liquibase.core;
    requires static com.mattbertolini.liquibase.logging.slf4j;
    requires static org.apache.commons.lang3;
    requires static org.yaml.snakeyaml;

    // Annotations
    requires static org.jetbrains.annotations;

    // Jackson
    requires com.fasterxml.jackson.databind;

    // Swagger
    requires io.swagger.v3.core;

    // No idea (is it necessary?)
    requires io.netty.transport;
    requires java.validation;

    // Lombok :D
    requires static lombok;
}
