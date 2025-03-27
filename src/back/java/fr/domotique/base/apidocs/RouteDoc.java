package fr.domotique.base.apidocs;

import lombok.*;
import org.jetbrains.annotations.*;

import java.lang.reflect.*;
import java.util.*;

/// Documentation for an API endpoint in OpenAPI format.
///
/// This class provides handy functions for defining the documentation
/// of a REST endpoint including operation ID, summary, description, request body,
/// parameters, responses, and tags.
///
/// Just chain setter methods and you're good to go!
///
/// Example usage:
/// ```java
/// RouteDoc doc = new RouteDoc("login")
///     .summary("Log in")
///     .description("Log in a user with email and password")
///     .requestBody(LoginInput.class, new LoginInput("example@mail.com", "password123"))
///     .response(200, CompleteUser.class, "Login successful")
///     .response(401, ErrorResponse.class, "Invalid credentials");
///```
public class RouteDoc {
    /// The operation ID for this route's OpenAPI documentation.
    @Nullable String operationId = null;

    /// A brief summary of what this API endpoint does.
    String summary = "";

    /// A longer description of the endpoint functionality.
    String description = "";

    /// The type of the request body this endpoint accepts.
    @Nullable Type requestBody = null; // TODO: More flexibility

    /// An example of the request body for documentation purposes.
    @Nullable Object requestBodyExample = null;

    /// The content type of the request body.
    String requestBodyType = "application/json";

    /// Tags for categorizing this endpoint in documentation.
    final List<String> tags = new ArrayList<>();

    /// The parameters this endpoint accepts (path, query, etc.).
    final List<ParamDoc> params = new ArrayList<>();

    /// The possible responses this endpoint can return.
    final List<ResponseDoc> responses = new ArrayList<>();

    /// Key used to add documentation in route metadata
    public static final String KEY = "Docs";

    public RouteDoc() {
    }

    public RouteDoc(@Nullable String operationId) {
        this.operationId = operationId;
    }

    // todo: move this elsewhere
    public static Type listType(Type inner) {
        return new ParameterizedType() {
            @Override
            public @NotNull Type @NotNull [] getActualTypeArguments() {
                return new Type[]{inner};
            }

            @Override
            public @NotNull Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (obj == null || getClass() != obj.getClass()) return false;
                ParameterizedType that = (ParameterizedType) obj;
                return Arrays.equals(getActualTypeArguments(), that.getActualTypeArguments()) &&
                        Objects.equals(getRawType(), that.getRawType()) &&
                        Objects.equals(getOwnerType(), that.getOwnerType());
            }
        };
    }

    public @Nullable String getOperationId() {
        return operationId;
    }

    public RouteDoc operationId(@Nullable String operationId) {
        this.operationId = operationId;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public RouteDoc summary(String summary) {
        this.summary = summary;
        return this;
    }

    public @Nullable Type getRequestBody() {
        return requestBody;
    }

    public RouteDoc requestBody(@Nullable Type requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    public RouteDoc requestBody(@Nullable Type requestBody, @Nullable Object requestBodyExample) {
        this.requestBody = requestBody;
        this.requestBodyExample = requestBodyExample;
        return this;
    }

    public RouteDoc requestBody(@Nullable Type requestBody, String requestBodyType) {
        this.requestBody = requestBody;
        this.requestBodyType = requestBodyType;
        return this;
    }

    public RouteDoc requestBody(@Nullable Type requestBody, String requestBodyType, @Nullable Object requestBodyExample) {
        this.requestBody = requestBody;
        this.requestBodyType = requestBodyType;
        this.requestBodyExample = requestBodyExample;
        return this;
    }

    public @Nullable Object getRequestBodyExample() {
        return requestBodyExample;
    }

    public RouteDoc requestBodyExample(@Nullable Object requestBodyExample) {
        this.requestBodyExample = requestBodyExample;
        return this;
    }

    public RouteDoc requestBodyType(String requestBodyType) {
        this.requestBodyType = requestBodyType;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public RouteDoc description(String description) {
        this.description = description;
        return this;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<ParamDoc> getParams() {
        return params;
    }

    public List<ResponseDoc> getResponses() {
        return responses;
    }

    public RouteDoc tag(String tag) {
        tags.add(tag);
        return this;
    }

    public RouteDoc param(ParamDoc param) {
        params.add(param);
        return this;
    }

    public RouteDoc param(ParamDoc.Location loc, String name, String description) {
        params.add(new ParamDoc().location(loc).name(name).desc(description));
        return this;
    }

    public RouteDoc param(ParamDoc.Location loc, String name, Type valType, String description) {
        params.add(new ParamDoc().location(loc).name(name).desc(description).valueType(valType));
        return this;
    }

    public RouteDoc pathParam(String name, String description) {
        params.add(new ParamDoc().name(name).desc(description));
        return this;
    }

    public RouteDoc pathParam(String name, Type valType, String description) {
        params.add(new ParamDoc().name(name).desc(description).valueType(valType));
        return this;
    }

    public RouteDoc queryParam(String name, String description) {
        params.add(new ParamDoc().location(ParamDoc.Location.QUERY).name(name).desc(description));
        return this;
    }

    public RouteDoc queryParam(String name, Type valType, String description) {
        params.add(new ParamDoc().location(ParamDoc.Location.QUERY).name(name).desc(description).valueType(valType));
        return this;
    }

    public RouteDoc optionalQueryParam(String name, Type valType, String description) {
        params.add(new ParamDoc().location(ParamDoc.Location.QUERY).name(name).desc(description).valueType(valType).required(false));
        return this;
    }

    public RouteDoc response(ResponseDoc response) {
        responses.add(response);
        return this;
    }

    public RouteDoc response(int status, String description) {
        responses.add(new ResponseDoc().status(status).description(description));
        return this;
    }

    public RouteDoc response(int status, Type content, String description) {
        responses.add(new ResponseDoc().status(status).description(description).content(content));
        return this;
    }

    public RouteDoc response(int status, Type content, String contentType, String description) {
        responses.add(new ResponseDoc().status(status).description(description).content(content).contentType(contentType));
        return this;
    }

    public RouteDoc response(int status, Type content, String description, Object example) {
        responses.add(new ResponseDoc().status(status).description(description).content(content).example(example));
        return this;
    }

    public RouteDoc response(int status, Type content, String contentType, String description, Object example) {
        responses.add(new ResponseDoc().status(status).description(description).content(content).contentType(contentType).example(example));
        return this;
    }
}
