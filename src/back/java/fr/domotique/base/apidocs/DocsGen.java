package fr.domotique.base.apidocs;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.datatype.jdk8.*;
import io.swagger.v3.core.util.*;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.examples.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.*;
import io.swagger.v3.oas.models.servers.*;
import io.vertx.core.json.jackson.*;
import io.vertx.ext.web.*;
import org.jetbrains.annotations.*;
import org.openapitools.jackson.nullable.*;

import java.lang.reflect.*;
import java.time.*;
import java.util.*;
import java.util.function.*;

/// Generates OpenAPI documentation based on Vert.x routes metadata.
public final class DocsGen {
    final Router router;
    final OpenAPI result = new OpenAPI(SpecVersion.V31)
        .openapi("3.1.0")
        .info(new Info()
            .title("Domotique API")
            .description("API documentation for the Domotique website.")
            .version("0.1.0"))
        .servers(List.of(new Server()
            .url("/")
            .description("Local server")))
        .components(new Components().schemas(new HashMap<>()))
        .paths(new Paths());
    final ObjectMapper mapper;

    HashMap<ClassKey, String> classToSchemaRef = new HashMap<>();

    static {
        // Also configure Optional<T> for the Yaml mapper
        Yaml.mapper().registerModule(new Jdk8Module().configureReadAbsentAsNull(true))
            .registerModule(new JsonNullableModule());

    }

    /// Creates a new doc generation session with the given router. Used internally by [#generate(Router)].
    private DocsGen(Router router, ObjectMapper mapper) {
        this.router = router;
        this.mapper = mapper;
    }

    /// Generates the OpenAPI documentation in YAML format.
    public static String generate(Router router) {
        ObjectMapper mapper1 = DatabindCodec.mapper();
        return new DocsGen(router, mapper1).run();
    }

    /// Generates the OpenAPI documentation. Must be only run once.
    private String run() {
        for (var r : router.getRoutes()) {
            visitRoute("", r, new ResponseDoc[0], new ParamDoc[0], new String[0]);
        }
        return Yaml.pretty(result);
    }

    /// Looks at a route to add documentation.
    ///
    /// @param prefix the prefix URL to apply to this route; never ends with a slash.
    /// @param route the route to document.
    /// @param inheritedParameters the parameters inherited from the parent routes. (-> [ParamDoc])
    /// @param inheritedTags the tags inherited from the parent routes. (-> [RouteDoc#getTags()])
    private void visitRoute(String prefix,
                            Route route,
                            ResponseDoc[] inheritedResponses,
                            ParamDoc[] inheritedParameters,
                            String[] inheritedTags) {
        RouteDoc rd = route.getMetadata(RouteDoc.KEY);

        // Merge responses, parameters and tags.
        ResponseDoc[] responses = mergeResponses(rd, inheritedResponses);
        ParamDoc[] params = mergeParams(rd, inheritedParameters);
        String[] tags = mergeTags(rd, inheritedTags);

        Router subRouter = route.getSubRouter();
        String subPath = route.getPath() == null ? "/" : route.getPath();

        if (subRouter != null) {
            String fullPath = normalizePrefix(prefix + subPath);

            // See if the sub router also has documentation, and merge what we can merge!
            RouteDoc subRd = subRouter.getMetadata(RouteDoc.KEY);
            if (subRd != null) {
                responses = mergeResponses(subRd, responses);
                params = mergeParams(subRd, params);
                tags = mergeTags(subRd, tags);
            }

            for (Route r : subRouter.getRoutes()) {
                visitRoute(fullPath, r, responses, params, tags);
            }
        } else {
            // Make sure that leaf route has some route documentation added.
            if (rd == null) {
                return;
            }

            // Calculate the full path.
            //  - the prefix never ends with a slash
            //  - the route path always starts with a slash
            // Concatenating them is safe!
            String fullPath = prefix + subPath;
            // Convert the Vert.x path (using :param syntax) to OpenAPI which uses {param} syntax.
            String oaPath = fullPath.replaceAll(":([^/]+)", "{$1}");

            // Convert the parameters to OpenAPI format.
            List<Parameter> newParamsOAI = Arrays.stream(params).map(this::toOpenAPI).toList();

            // Create the request body if there's one
            RequestBody requestBodyOAI = null;
            if (rd.requestBody != null) {
                // Find the schema, and create a media type for it.
                Schema<?> schema = schemaForReflect(rd.requestBody, rd.requestBodyFullyOptional);
                MediaType media = new MediaType().schema(schema);

                // Add an example if there is one.
                if (rd.requestBodyExample != null) {
                    media.setExample(rd.requestBodyExample);
                }

                // Make it! (it's required by default)
                requestBodyOAI = new RequestBody()
                    .content(new Content().addMediaType(rd.requestBodyType, media))
                    .required(true);
            }

            // Create the response object, with all responses from the RouteDoc.
            ApiResponses apiResponses = new ApiResponses();
            for (ResponseDoc response : responses) {
                apiResponses.put(Integer.toString(response.getStatus()), toOpenAPI(response));
            }

            // Register the route to all possible methods. In practice, it's just one so this for loop is misleading.
            PathItem pathItem = findPathItem(oaPath, inheritedParameters);
            for (var method : route.methods()) {
                var pathItemMethod = PathItem.HttpMethod.valueOf(method.name());
                pathItem
                    .operation(pathItemMethod, new Operation()
                        .operationId(rd.operationId)
                        .summary(rd.summary)
                        .description(rd.description)
                        .tags(Arrays.asList(tags))
                        .parameters(newParamsOAI)
                        .responses(apiResponses)
                        .requestBody(requestBodyOAI));
            }
        }
    }

    private static ResponseDoc[] mergeResponses(@Nullable RouteDoc rd,
                                                ResponseDoc[] activeResponses) {
        if (rd == null) {
            return activeResponses;
        }

        // Create a map of responses by status code to allow overriding
        Map<Integer, ResponseDoc> responseMap = new TreeMap<>();

        // Add existing responses to the map
        for (ResponseDoc response : activeResponses) {
            responseMap.put(response.getStatus(), response);
        }

        // Override with new responses that have the same status code
        for (ResponseDoc response : rd.responses) {
            responseMap.put(response.getStatus(), response);
        }

        // Convert back to array
        return responseMap.values().toArray(new ResponseDoc[0]);
    }

    private static ParamDoc[] mergeParams(@Nullable RouteDoc rd,
                                          ParamDoc[] activeParameters) {
        if (rd == null) {
            return activeParameters;
        }

        // Create a map of parameters by location and name to allow overriding
        Map<String, ParamDoc> paramMap = new HashMap<>();

        // Add existing parameters to the map with a composite key of "location:name"
        for (ParamDoc param : activeParameters) {
            String key = param.location + ":" + param.name;
            paramMap.put(key, param);
        }

        // Override with new parameters that have the same location and name
        for (ParamDoc param : rd.params) {
            String key = param.location + ":" + param.name;
            paramMap.put(key, param);
        }

        // Convert back to array
        return paramMap.values().toArray(new ParamDoc[0]);
    }

    private static String[] mergeTags(@Nullable RouteDoc rd, String[] inherited) {
        if (rd == null) {
            return inherited;
        }

        var tags = new ArrayList<>(Arrays.asList(inherited));
        for (String newTag : rd.tags) {
            if (tags.contains(newTag)) {
                continue;
            }
            tags.add(newTag);
        }
        return tags.toArray(new String[0]);
    }

    private String normalizePrefix(String p) {
        if (p.endsWith("/*")) {
            return p.substring(0, p.length() - 2);
        } else if (p.endsWith("*")) {
            return p.substring(0, p.length() - 1);
        } else {
            return p;
        }
    }

    private PathItem findPathItem(String oaPath, ParamDoc[] inheritedParams) {
        return result.getPaths().computeIfAbsent(oaPath, _ -> {
            var pi = new PathItem();
            for (ParamDoc pd : inheritedParams) {
                pi.addParametersItem(toOpenAPI(pd)); // todo: ehh
            }
            return pi;
        });
    }

    private Parameter toOpenAPI(ParamDoc pd) {
        Schema<?> schema = schemaForReflect(pd.valueType, false);
        if (pd.valueType == String.class) {
            if (pd.format == ParamDoc.Format.DATE) {
                schema.format("date");
            } else if (pd.format == ParamDoc.Format.DATE_TIME) {
                schema.format("date-time");
            }
        }

        return new Parameter()
            .name(pd.name)
            .in(pd.location.toString().toLowerCase())
            .description(pd.desc)
            .required(pd.required)
            .schema(schema);
    }

    private ApiResponse toOpenAPI(ResponseDoc rd) {
        Content content = null;
        if (rd.content != null) {
            Schema<?> schema = schemaForReflect(rd.content, false);
            MediaType media = new MediaType().schema(schema);
            if (rd.example != null) {
                media.addExamples("main", new Example().value(rd.example).summary(rd.description));
            }
            content = new Content().addMediaType(rd.contentType, media);
        }

        return new ApiResponse()
            .description(rd.description)
            .content(content);
    }

    /*
     ---- Schema generation ----
     */

    /// Map of Java types to their corresponding OpenAPI schemas
    private static final Map<Class<?>, Supplier<Schema<?>>> SIMPLE_TYPE_SCHEMAS = Map.ofEntries(
        // Object
        Map.entry(Object.class, ObjectSchema::new),

        // Integer types
        Map.entry(Integer.class, IntegerSchema::new),
        Map.entry(int.class, IntegerSchema::new),
        Map.entry(Long.class, IntegerSchema::new),
        Map.entry(long.class, IntegerSchema::new),
        Map.entry(Short.class, IntegerSchema::new),
        Map.entry(short.class, IntegerSchema::new),
        Map.entry(Byte.class, IntegerSchema::new),
        Map.entry(byte.class, IntegerSchema::new),

        // Floating point types
        Map.entry(Float.class, NumberSchema::new),
        Map.entry(float.class, NumberSchema::new),
        Map.entry(Double.class, NumberSchema::new),
        Map.entry(double.class, NumberSchema::new),

        // Other basic types
        Map.entry(String.class, StringSchema::new),
        Map.entry(Boolean.class, BooleanSchema::new),
        Map.entry(boolean.class, BooleanSchema::new),

        // Date/time types
        Map.entry(Instant.class, () -> new StringSchema().format("date-time")),
        Map.entry(LocalDate.class, () -> new StringSchema().format("date")),

        // File types
        Map.entry(FileUpload.class, FileSchema::new)
    );

    private Schema<?> schemaForReflect(Type type, boolean optionalVariant) {
        if (type instanceof JavaType t) {
            return schemaForJackson(t, optionalVariant);
        }

        return schemaForJackson(mapper.constructType(type), optionalVariant);
    }

    private Schema<?> schemaForJackson(JavaType type, boolean optionalVariant) {
        if (type instanceof SimpleType st) {
            var clazz = st.getRawClass();

            if (clazz == Optional.class || clazz == JsonNullable.class) {
                // If the type is Optional<T>, we need to get the type of T
                var innerType = st.getBindings().getBoundType(0);
                return schemaForJackson(innerType, true);
            }

            // If we've already defined a schema for this class, create a $ref schema.
            var key = new ClassKey(clazz, optionalVariant);
            if (classToSchemaRef.containsKey(key)) {
                return new Schema<>().$ref(classToSchemaRef.get(key));
            }

            // Use the schema mapper to find the appropriate schema
            if (SIMPLE_TYPE_SCHEMAS.containsKey(clazz)) {
                return SIMPLE_TYPE_SCHEMAS.get(clazz).get();
            } else {
                String schemaRef = registerNewSchema(key.clazz, key.optionalVariant);
                return new Schema<>().$ref(schemaRef);
            }
        } else if (type instanceof ArrayType at) {
            return new ArraySchema().items(schemaForJackson(at.getContentType(), optionalVariant));
        } else if (type instanceof CollectionType at) {
            return new ArraySchema().items(schemaForJackson(at.getContentType(), optionalVariant));
        } else if (type instanceof MapType mt) {
            return new MapSchema().additionalProperties(schemaForJackson(mt.getContentType(), optionalVariant));
        }

        throw new UnsupportedOperationException("Cannot generate schema for " + type);
    }

    /// Registers a new OpenAPI schema for a simple type class (i.e. a POJO, an enum, or a record; not lists or maps).
    ///
    /// This is also where DocAlias'd classes get "flattened" out.
    private String registerNewSchema(Class<?> clazz, boolean optionalVariant) {
        var schemas = result.getComponents().getSchemas();

        // Find the documentation name of the class using the DocName annotation.
        String docsName = getDocsName(clazz, optionalVariant);
        ApiDoc classAnn = clazz.getAnnotation(ApiDoc.class);
        Schema<?> existingSchema;
        Schema<?> newSchema;

        if (clazz.isEnum()) {
            // Enum case -> Make a string schema with the enum values
            @SuppressWarnings("unchecked")
            var enumClass = (Class<? extends Enum<?>>) clazz;

            // I have no idea why the function has an underscore
            newSchema = new StringSchema()._enum(
                Arrays.stream(enumClass.getEnumConstants())
                    .map(Enum::name)
                    .toList()
            );

            // Submit it to the schemas in the YAML file
            existingSchema = schemas.putIfAbsent(docsName, newSchema);
        } else {
            // Unwrap any DocAlias<T> type (to get the T)
            Type unaliasedType = clazz; // = T from DocAlias<T>
            if (clazz.getSuperclass() == DocAlias.class) {
                unaliasedType = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
            }

            // Kindly ask Jackson to tell us how it would serialize this class in JSON.
            var beanProps = mapper.getSerializationConfig().introspect(mapper.constructType(unaliasedType)).findProperties();

            // Create a new schema with the properties of the class
            @SuppressWarnings("rawtypes")
            Map<String, Schema> props = new HashMap<>();
            List<String> requiredProps = new ArrayList<>();
            newSchema = new ObjectSchema().properties(props);

            // Register the example object from the EXAMPLE static field, if any.
            try {
                // Find the EXAMPLE field (if it's from DocAlias, take it from the DocAlias class)
                var example = getExampleField(clazz);

                // Make sure it is a static field and that it exists
                if (example != null && (example.getModifiers() & Modifier.STATIC) != 0) {
                    // Hack into it and retrieve its value
                    example.setAccessible(true);
                    Object val = example.get(null); // TODO: Validate same type
                    newSchema.setExample(val);
                }
            } catch (IllegalAccessException e) {
                // Huh?
                throw new RuntimeException(e);
            }

            // Register all properties
            for (BeanPropertyDefinition rc : beanProps) {
                // TODO: Fix possible cycles with self referencing classes
                // Find the name & Schema for the property's type. Child properties don't propagate optionality.
                String propName = rc.getName();

                var type = rc.getPrimaryType();
                Schema<?> propSchema = schemaForJackson(rc.getPrimaryType(), false);

                // Register the description if we do have one.
                ApiDoc descAnn = rc.getPrimaryMember().getAnnotation(ApiDoc.class);
                if (descAnn != null) {
                    propSchema.description(descAnn.value());
                }

                // It's optional if the property annotation is, or if the class annotation has optional == true.
                // Also, don't add it to the required list of it's an Optional<T> type.
                if ((descAnn == null || !descAnn.optional())
                    && (classAnn == null || !classAnn.optional())
                    && !optionalVariant
                    && (!(type instanceof SimpleType st) || st.getRawClass() != Optional.class && st.getRawClass() != JsonNullable.class)) {
                    requiredProps.add(propName);
                }

                // Register the property
                props.put(propName, propSchema);
            }

            // Set all required properties
            newSchema.setRequired(requiredProps);

            // Register the schema
            existingSchema = schemas.putIfAbsent(docsName, newSchema);
        }

        if (existingSchema != null) {
            throw new IllegalArgumentException("Schema " + docsName + " already exists (class: " + clazz + ")");
        }

        // Register the description of the class if we do have one.
        if (classAnn != null) {
            // Put it in a stringbuilder for later if it's an enum.
            StringBuilder doc = new StringBuilder(classAnn.value());

            // OpenAPI doesn't support documentation on enums so we'll glue them to the documentation instead.
            if (clazz.isEnum()) {
                for (Field f : clazz.getFields()) {
                    ApiDoc enumDoc = f.getAnnotation(ApiDoc.class);
                    if (enumDoc != null) {
                        doc.append("\n- `").append(f.getName()).append("`: ").append(enumDoc.value());
                    }
                }
            }

            // Register the description now
            newSchema.description(doc.toString());
        }

        String reference = "#/components/schemas/" + docsName;
        classToSchemaRef.put(new ClassKey(clazz, optionalVariant), reference);
        return reference;
    }

    record ClassKey(Class<?> clazz, boolean optionalVariant) {}

    private @Nullable Field getExampleField(Class<?> clazz) {
        try {
            return clazz.getDeclaredField("EXAMPLE");
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private String getDocsName(Class<?> clazz, boolean optionalVariant) {
        DocName ann = clazz.getAnnotation(DocName.class);
        if (ann != null) {
            return ann.value() + (optionalVariant ? "Optional" : "");
        } else {
            return clazz.getSimpleName() + (optionalVariant ? "Optional" : "");
        }
    }
}
