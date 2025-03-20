package fr.domotique.apidocs;

import java.lang.reflect.*;

/// Document about a parameter in an API endpoint (path, header, query) for OpenAPI docs.
///
/// @see RouteDoc
public class ParamDoc {
    /// The location where this parameter appears in the request (path, query, or header)
    Location location = Location.PATH;
    /// The name of the parameter
    String name = "";
    /// Description of the parameter for documentation purposes
    String desc = "";
    /// The data type of this parameter
    Type valueType = String.class;

    /// Default constructor
    public ParamDoc() {
    }

    /// Gets the location of this parameter
    /// @return The parameter location (path, query or header)
    public Location getLocation() {
        return location;
    }

    /// Sets the location of this parameter
    /// @param location The parameter location
    /// @return this instance for method chaining
    public ParamDoc location(Location location) {
        this.location = location;
        return this;
    }

    /// Gets the name of this parameter
    /// @return The parameter name
    public String getName() {
        return name;
    }

    /// Sets the name of this parameter
    /// @param name The parameter name
    /// @return this instance for method chaining
    public ParamDoc name(String name) {
        this.name = name;
        return this;
    }

    /// Gets the description of this parameter
    /// @return The parameter description
    public String getDesc() {
        return desc;
    }

    /// Sets the description of this parameter
    /// @param desc The parameter description
    /// @return this instance for method chaining
    public ParamDoc desc(String desc) {
        this.desc = desc;
        return this;
    }

    /// Gets the value type of this parameter
    /// @return The parameter value type
    public Type getValueType() {
        return valueType;
    }

    /// Sets the value type of this parameter
    /// @param valueType The parameter value type
    /// @return this instance for method chaining
    public ParamDoc valueType(Type valueType) {
        this.valueType = valueType;
        return this;
    }

    /// Where the parameter is located in the request.
    public enum Location {
        /// As a path parameter: `/api/users/:userId`
        PATH,
        /// As a query parameter: `/api/users?name=John`
        QUERY,
        /// As a header parameter: `Authorization: Bearer ...`
        HEADER
    }
}
