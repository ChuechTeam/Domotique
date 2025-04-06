package fr.domotique.base.apidocs;

import lombok.*;
import lombok.experimental.*;

import java.lang.reflect.*;

/// Document about a parameter in an API endpoint (path, header, query) for OpenAPI docs.
///
/// @see RouteDoc
@Accessors(fluent = true)
@Getter
@Setter
public class ParamDoc {
    /// The location where this parameter appears in the request (path, query, or header)
    Location location = Location.PATH;
    /// The name of the parameter
    String name = "";
    /// Description of the parameter for documentation purposes
    String desc = "";
    /// The data type of this parameter
    Type valueType = String.class;
    /// The format of the parameter (e.g., date, date-time)
    Format format = Format.NONE;
    /// Whether this parameter is required or optional
    boolean required = true;

    /// Where the parameter is located in the request.
    public enum Location {
        /// As a path parameter: `/api/users/:userId`
        PATH,
        /// As a query parameter: `/api/users?name=John`
        QUERY,
        /// As a header parameter: `Authorization: Bearer ...`
        HEADER
    }
    public enum Format {
        NONE,
        DATE,
        DATE_TIME,
    }
}
