package fr.domotique.base.apidocs;

import java.lang.annotation.*;

/// Specifies a description for a field, method, or class in the API documentation.
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiDoc {
    String value() default "";

    String fileType() default "";
}
