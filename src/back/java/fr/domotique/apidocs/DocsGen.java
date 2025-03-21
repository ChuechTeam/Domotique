package fr.domotique.apidocs;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.type.*;
import io.swagger.v3.core.util.*;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.examples.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.*;
import io.swagger.v3.oas.models.servers.*;
import io.vertx.ext.web.*;
import org.jetbrains.annotations.*;

import java.lang.reflect.*;
import java.time.*;
import java.util.*;

/// Generates OpenAPI documentation based on Vert.x routes metadata.
public final class DocsGen {
    final Router router;
    final OpenAPI result = new OpenAPI(SpecVersion.V31)
        .info(new Info()
            .title("Domotique API")
            .description("API documentation for the Domotique website.")
            .version("0.1.0"))
        .servers(List.of(new Server()
            .url("/") // TODO: Adapt to port settings
            .description("Local server")))
        .components(new Components().schemas(new HashMap<>()))
        .paths(new Paths());
    final ObjectMapper mapper = new ObjectMapper();

    HashMap<Class<?>, String> classToSchemaRef = new HashMap<>();

    /// Creates a new doc generation session with the given router. Used internally by [#generate(Router)].
    private DocsGen(Router router) {
        this.router = router;
    }

    /// Generates the OpenAPI documentation in YAML format.
    public static String generate(Router router) {
        return new DocsGen(router).run();
    }

    /// Generates the OpenAPI documentation. Must be only run once.
    private String run() {
        for (var r : router.getRoutes()) {
            visitRoute("", r, new ParamDoc[0], new String[0]);
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
                            ParamDoc[] inheritedParameters,
                            String[] inheritedTags) {
        RouteDoc rd = route.getMetadata(RouteDoc.KEY);

        Router subRouter = route.getSubRouter();
        if (subRouter != null) {
            String fullPath = normalizePrefix(prefix + route.getPath());

            ParamDoc[] params = mergeParams(rd, inheritedParameters, fullPath, null);
            String[] tags = mergeTags(rd, inheritedTags);

            for (Route r : subRouter.getRoutes()) {
                visitRoute(fullPath, r, params, tags);
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
            String fullPath = prefix + route.getPath();
            // Convert the Vert.x path (using :param syntax) to OpenAPI which uses {param} syntax.
            String oaPath = fullPath.replaceAll(":([^/]+)", "{$1}");

            // Prepare the lists & arrays for the tags and parameters.
            List<ParamDoc> newParams = new ArrayList<>();
            String[] allTags;

            // Merge new and inherited parameters; we're interested ONLY IN NEW PARAMETERS.
            mergeParams(rd, inheritedParameters, fullPath, newParams);
            // Merge new and inherited tags; we're interested IN ALL TAGS.
            allTags = mergeTags(rd, inheritedTags);

            // Convert the parameters to OpenAPI format.
            List<Parameter> newParamsOAI = newParams.stream().map(this::toOpenAPI).toList();

            RequestBody requestBodyOAI = null;
            if (rd.requestBody != null) {
                Schema<?> schema = schemaForReflect(rd.requestBody);
                MediaType media = new MediaType().schema(schema);
                if (rd.requestBodyExample != null) {
                    media.setExample(rd.requestBodyExample);
                }
                requestBodyOAI = new RequestBody()
                    .content(new Content().addMediaType("application/json",
                        media))
                    .required(true);
            }

            // Create the response object, with all responses from the RouteDoc.
            ApiResponses responses = new ApiResponses();
            for (ResponseDoc response : rd.responses) {
                responses.put(Integer.toString(response.getStatus()), toOpenAPI(response));
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
                        .tags(Arrays.asList(allTags))
                        .parameters(newParamsOAI)
                        .responses(responses)
                        .requestBody(requestBodyOAI));
            }
        }
    }

    // TODO: Make this algorithm less dumb since we recalculate things of the full path.
    private static ParamDoc[] mergeParams(@Nullable RouteDoc rd,
                                          ParamDoc[] activeParameters,
                                          String fullPath,
                                          @Nullable List<ParamDoc> outNewParams) {
        var parts = fullPath.split("/");

        var newActiveParameters = new ArrayList<>(Arrays.asList(activeParameters));

        for (String p : parts) {
            if (p.startsWith(":")) {
                String paramName = p.substring(1);
                boolean isNewParameter = true;
                for (ParamDoc pd : activeParameters) {
                    if (pd.name.equals(paramName)) {
                        isNewParameter = false;
                        break;
                    }
                }

                if (isNewParameter) {
                    ParamDoc newParam = null;
                    // First see if the RouteDoc provides one already
                    if (rd != null) {
                        newParam = rd.params.stream()
                            .filter(pd -> pd.location == ParamDoc.Location.PATH && pd.name.equals(paramName))
                            .findFirst()
                            .orElse(null);
                    }

                    // Else, create one with default values.
                    if (newParam == null) {
                        newParam = new ParamDoc()
                            .location(ParamDoc.Location.PATH)
                            .name(paramName)
                            .valueType(String.class);
                    }

                    newActiveParameters.add(newParam);
                    if (outNewParams != null) {
                        outNewParams.add(newParam);
                    }
                }
            }
        }

        return newActiveParameters.toArray(new ParamDoc[0]);
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
        return new Parameter()
            .name(pd.name)
            .in(pd.location.toString().toLowerCase())
            .description(pd.desc)
            .required(true)
            .schema(schemaForReflect(pd.valueType));
    }

    private ApiResponse toOpenAPI(ResponseDoc rd) {
        Content content = null;
        if (rd.content != null) {
            Schema<?> schema = schemaForReflect(rd.content);
            MediaType media = new MediaType().schema(schema);
            if (rd.example != null) {
                media.addExamples("main", new Example().value(rd.example).summary(rd.description));
            }
            content = new Content().addMediaType("application/json", media);
        }

        return new ApiResponse()
            .description(rd.description)
            .content(content);
    }

    /*
     ---- Schema generation ----
     */

    private Schema<?> schemaForReflect(Type type) {
        if (type instanceof JavaType t) {
            return schemaForJackson(t);
        }

        return schemaForJackson(mapper.constructType(type));
    }

    private Schema<?> schemaForJackson(JavaType type) {
        if (type instanceof SimpleType st) {
            var clazz = st.getRawClass();

            if (classToSchemaRef.containsKey(clazz)) {
                return new Schema<>().$ref(classToSchemaRef.get(clazz));
            }

            if (clazz.equals(Integer.class)
                || clazz.equals(int.class)
                || clazz.equals(Long.class)
                || clazz.equals(long.class)
                || clazz.equals(Short.class)
                || clazz.equals(short.class)
                || clazz.equals(Byte.class)
                || clazz.equals(byte.class)) {
                return new IntegerSchema();
            } else if (clazz.equals(Float.class)
                || clazz.equals(float.class)
                || clazz.equals(Double.class)
                || clazz.equals(double.class)) {
                return new NumberSchema();
            } else if (clazz.equals(String.class)) {
                return new StringSchema();
            } else if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
                return new BooleanSchema();
            } else if (clazz.equals(Instant.class)) {
                return new StringSchema().format("date-time");
            } else if (clazz.equals(LocalDate.class)) {
                return new StringSchema().format("date");
            } else if (clazz.equals(Object.class)) {
                return new ObjectSchema();
            } else {
                String schemaRef = registerNewSchema(clazz);
                return new Schema<>().$ref(schemaRef);
            }
        } else if (type instanceof ArrayType at) {
            return new ArraySchema().items(schemaForJackson(at.getContentType()));
        } else if (type instanceof MapType mt) {
            return new MapSchema().additionalProperties(schemaForJackson(mt.getContentType()));
        }

        throw new UnsupportedOperationException("Cannot generate schema for " + type);
    }

    /// Registers a new OpenAPI schema for a simple type class (i.e. a POJO, an enum, or a record; not lists or maps).
    ///
    /// This is also where DocAlias'd classes get "flattened" out.
    private String registerNewSchema(Class<?> clazz) {
        var schemas = result.getComponents().getSchemas();

        // Find the documentation name of the class using the DocName annotation.
        String docsName = getDocsName(clazz);
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
                // Find the Schema for the property's type
                Schema<?> propSchema = schemaForJackson(rc.getPrimaryType());

                // Register the description if we do have one.
                DocDesc descAnn = rc.getPrimaryMember().getAnnotation(DocDesc.class);
                if (descAnn != null) {
                    propSchema.description(descAnn.value());
                }

                // Register the property
                props.put(rc.getName(), propSchema);
            }

            // Register the schema
            existingSchema = schemas.putIfAbsent(docsName, newSchema);
        }

        if (existingSchema != null) {
            throw new IllegalArgumentException("Schema " + docsName + " already exists (class: " + clazz + ")");
        }

        // Register the description of the class if we do have one.
        DocDesc descAnn = clazz.getAnnotation(DocDesc.class);
        if (descAnn != null) {
            newSchema.description(descAnn.value());
        }

        String reference = "#/components/schemas/" + docsName;
        classToSchemaRef.put(clazz, reference);
        return reference;
    }

    private @Nullable Field getExampleField(Class<?> clazz) {
        try {
            return clazz.getDeclaredField("EXAMPLE");
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private String getDocsName(Class<?> clazz) {
        DocName ann = clazz.getAnnotation(DocName.class);
        if (ann != null) {
            return ann.value();
        } else {
            return clazz.getSimpleName();
        }
    }
}
