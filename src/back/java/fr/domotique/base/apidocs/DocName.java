package fr.domotique.base.apidocs;

import java.lang.annotation.*;

/// Defines the name of an API model in the API documentation (of OpenAPI).
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DocName {
    String value();
}
