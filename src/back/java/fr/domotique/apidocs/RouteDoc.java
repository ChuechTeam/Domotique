package fr.domotique.apidocs;

import org.jetbrains.annotations.*;

import java.lang.reflect.*;
import java.util.*;

/// Documentation about an API endpoint, for OpenAPI docs.
public class RouteDoc {
    @Nullable String operationId = null;
    String summary = "";
    String description = "";
    @Nullable Type requestBody = null; // TODO: More flexibility
    @Nullable Object requestBodyExample = null;
    final List<String> tags = new ArrayList<>();
    final List<ParamDoc> params = new ArrayList<>();
    final List<ResponseDoc> responses = new ArrayList<>();

    public static final String KEY = "Docs";

    public RouteDoc() {
    }

    public RouteDoc(@Nullable String operationId) {
        this.operationId = operationId;
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

    public @Nullable Object getRequestBodyExample() {
        return requestBodyExample;
    }

    public RouteDoc requestBodyExample(@Nullable Object requestBodyExample) {
        this.requestBodyExample = requestBodyExample;
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

    public RouteDoc param(String name, String description) {
        params.add(new ParamDoc().name(name).desc(description));
        return this;
    }

    public RouteDoc param(String name, Type valType, String description) {
        params.add(new ParamDoc().name(name).desc(description).valueType(valType));
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

    public RouteDoc response(int status, Type content, String description, Object example) {
        responses.add(new ResponseDoc().status(status).description(description).content(content).example(example));
        return this;
    }
}
