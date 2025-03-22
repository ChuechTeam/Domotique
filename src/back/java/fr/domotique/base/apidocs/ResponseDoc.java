package fr.domotique.base.apidocs;

import org.jetbrains.annotations.*;

import java.lang.reflect.*;

/// Documentation about a possible response from an API endpoint, for OpenAPI docs.
///
/// [OpenAPI Docs](https://swagger.io/docs/specification/v3_0/describing-responses/)
public class ResponseDoc {
    int status = 200;
    String description = "";
    @Nullable Type content = null; // TODO: Change this to give more flexibility (i.e. submitting files)
    String contentType = "application/json";
    @Nullable Object example = null;

    public int getStatus() {
        return status;
    }

    public ResponseDoc status(int status) {
        this.status = status;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ResponseDoc description(String description) {
        this.description = description;
        return this;
    }

    public @Nullable Type getContent() {
        return content;
    }

    public ResponseDoc content(@Nullable Type content) {
        this.content = content;
        return this;
    }

    public ResponseDoc content(@Nullable Type content, @Nullable Object example) {
        this.content = content;
        this.example = example;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public ResponseDoc contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public @Nullable Object getExample() {
        return example;
    }

    public ResponseDoc example(@Nullable Object example) {
        this.example = example;
        return this;
    }
}
