package fr.domotique.api.devicetypes;

import fr.domotique.*;
import fr.domotique.api.devices.*;
import fr.domotique.base.*;
import fr.domotique.base.Validation;
import fr.domotique.base.apidocs.*;
import fr.domotique.data.*;
import io.vertx.core.Future;
import io.vertx.core.http.*;
import io.vertx.ext.web.*;
import org.slf4j.*;

import java.util.*;

/// All API endpoints to access device type data
public class DeviceTypeSection extends Section {
    /// Creates a new DeviceTypeSection with the given server.
    public DeviceTypeSection(Server server) {
        super(server);
    }

    // All routes of this section will begin with /api/device-types
    static final String PATH_PREFIX = "/api/device-types*";

    // Logger for this section
    static final Logger log = LoggerFactory.getLogger(DeviceTypeSection.class);

    @Override
    public void register(Router router) {
        // Create a sub-router for all device type routes
        var deviceTypeRoutes = newRouter();

        // When:
        // - A user requests data, they must be AT LEAST a BEGINNER (= confirmed email)
        // - A user modifies data, they must be AT LEAST an ADVANCED user
        deviceTypeRoutes.route().handler(ctx -> {
            Authenticator auth = Authenticator.get(ctx);

            if (ctx.request().method() == HttpMethod.GET) {
                auth.requireAuth(Level.BEGINNER);
            } else {
                auth.requireAuth(Level.ADVANCED);
            }

            // Don't stop there! Continue the request.
            ctx.next();
        });

        // Register all device type-related endpoints
        deviceTypeRoutes.get("/").respond(this::getAll).putMetadata(RouteDoc.KEY, GET_DEVICE_TYPES_DOC);
        deviceTypeRoutes.post("/").respond(vt(this::createDeviceType)).putMetadata(RouteDoc.KEY, CREATE_DEVICE_TYPE_DOC);

        // Routes with parameters come last
        deviceTypeRoutes.get("/:deviceTypeId").respond(this::getDeviceTypeById).putMetadata(RouteDoc.KEY, GET_DEVICE_TYPE_DOC);
        deviceTypeRoutes.put("/:deviceTypeId").respond(vt(this::updateDeviceType)).putMetadata(RouteDoc.KEY, UPDATE_DEVICE_TYPE_DOC);
        deviceTypeRoutes.delete("/:deviceTypeId").respond(vt(this::deleteDeviceType)).putMetadata(RouteDoc.KEY, DELETE_DEVICE_TYPE_DOC);

        // Register the sub-router with the main router
        // And add common authorization responses to the documentation.
        doc(router.route(PATH_PREFIX).subRouter(deviceTypeRoutes))
            .tag("Device Types")
            .response(401, ErrorResponse.class, "You are not logged in.")
            .response(403, ErrorResponse.class, "You don't have permission to access this resource.");
    }

    // region GET /api/device-types | Get all device types
    static final RouteDoc GET_DEVICE_TYPES_DOC = new RouteDoc("getDeviceTypes")
        .summary("Get device types")
        .description("Gets all device types from the database.")
        .optionalQueryParam("ids", int[].class, "The ids of devices to look for. If empty, all device types are returned.")
        .optionalQueryParam("name", String.class, "The name of the device type to look for. If empty, all device types are returned.")
        .response(200, DeviceTypesResponse.class, "The list of all device types.");

    record DeviceTypesResponse(List<CompleteDeviceType> deviceTypes) {}

    Future<DeviceTypesResponse> getAll(RoutingContext context) {
        List<Integer> ids = readIntListFromQueryParams(context, "ids");
        String name = context.queryParams().get("name");

        Future<List<DeviceType>> devicesFuture;
        if (!ids.isEmpty()) {
            devicesFuture = server.db().deviceTypes().getAll(ids);
        } else if (name != null && !name.isEmpty()) {
            devicesFuture = server.db().deviceTypes().getAllByName(name);
        } else {
            devicesFuture = server.db().deviceTypes().getAll();
        }

        return devicesFuture
            .map(deviceTypes -> deviceTypes.stream()
                .map(CompleteDeviceType::fromDeviceType)
                .toList())
            .map(DeviceTypesResponse::new);
    }
    // endregion

    // region GET /api/device-types/:deviceTypeId | Get device type by ID
    static final RouteDoc GET_DEVICE_TYPE_DOC = new RouteDoc("getDeviceTypeById")
        .summary("Get device type by ID")
        .description("Gets a device type by its ID.")
        .pathParam("deviceTypeId", int.class, "The ID of the device type.")
        .response(200, CompleteDeviceType.class, "The device type data.")
        .response(204, "Device type not found.");

    Future<CompleteDeviceType> getDeviceTypeById(RoutingContext context) {
        int deviceTypeId = readIntPathParam(context, "deviceTypeId");
        return server.db().deviceTypes().get(deviceTypeId)
            .map(CompleteDeviceType::fromDeviceType);
    }
    // endregion

    @ApiDoc("Data for both INSERT and UPDATE operations on a device type.")
    record DeviceTypeInput(String name, DeviceCategory category, List<AttributeType> attributes) {
        public DeviceTypeInput {
            name = Sanitize.string(name);
        }

        /// Runs validation for this input.
        public void validate() {
            try (var block = Validation.start()) {
                Validation.lengthIn(block, "name", name, 1, 128,
                    "Le nom est vide.",
                    "Le nom est trop long.");

                if (attributes == null || attributes.isEmpty()) {
                    block.addError("attributes", "La liste d'attributs ne peut pas être vide.");
                }
            }
        }

        public EnumSet<AttributeType> attributesAsEnumSet() {
            return attributes != null && !attributes.isEmpty()
                ? EnumSet.copyOf(attributes)
                : EnumSet.noneOf(AttributeType.class);
        }
    }

    // region POST /api/device-types | Create device type
    static final RouteDoc CREATE_DEVICE_TYPE_DOC = new RouteDoc("createDeviceType")
        .summary("Create device type")
        .description("Creates a new device type.")
        .requestBody(DeviceTypeInput.class, new DeviceTypeInput(
            "Smart Watch",
            DeviceCategory.HEALTH,
            List.of(AttributeType.TEMPERATURE, AttributeType.HUMIDITY)))
        .response(201, CompleteDeviceType.class, "The device type was created successfully.")
        .response(422, ErrorResponse.class, "Some fields are invalid or the device type name already exists.");

    CompleteDeviceType createDeviceType(RoutingContext context) {
        DeviceTypeInput input = readBody(context, DeviceTypeInput.class);

        // Validate data
        input.validate();

        // Create the device type
        DeviceType deviceType = new DeviceType(0, input.name, input.category, input.attributesAsEnumSet());

        server.db().deviceTypes().insert(deviceType).await();
        log.info("Device type created with id {} and name {}", deviceType.getId(), deviceType.getName());

        context.response().setStatusCode(201);
        return CompleteDeviceType.fromDeviceType(deviceType);
    }
    // endregion

    // region POST /api/device-types/:deviceTypeId | Update device type
    static final RouteDoc UPDATE_DEVICE_TYPE_DOC = new RouteDoc("updateDeviceType")
        .summary("Update device type")
        .description("Updates an existing device type.")
        .pathParam("deviceTypeId", int.class, "The ID of the device type to update.")
        .requestBody(DeviceTypeInput.class, new DeviceTypeInput(
            "Updated Smart Watch",
            DeviceCategory.HEALTH,
            List.of(AttributeType.TEMPERATURE, AttributeType.HUMIDITY, AttributeType.CALORIES_BURNED)))
        .response(200, CompleteDeviceType.class, "The device type was updated successfully.")
        .response(404, ErrorResponse.class, "Device type not found.")
        .response(422, ErrorResponse.class, "Some fields are invalid.");

    CompleteDeviceType updateDeviceType(RoutingContext context) {
        int deviceTypeId = readIntPathParam(context, "deviceTypeId");
        DeviceTypeInput input = readBody(context, DeviceTypeInput.class);

        // Validate device type input
        input.validate();

        // Get the device type
        DeviceType deviceType = server.db().deviceTypes().get(deviceTypeId).await();
        if (deviceType == null) {
            throw new RequestException("Type d'appareil introuvable.", 404, "DEVICE_TYPE_NOT_FOUND");
        }

        // Compute the attribute set, and see if they changed.
        EnumSet<AttributeType> newAttributes = input.attributesAsEnumSet();
        boolean attributesChanged = !deviceType.getAttributes().equals(newAttributes);

        // Update device type properties
        deviceType.setName(input.name);
        deviceType.setAttributes(newAttributes);
        deviceType.setCategory(input.category);

        // Submit changes to the database
        server.db().deviceTypes().update(deviceType).await();
        log.info("Device type updated with id {} and name {}", deviceType.getId(), deviceType.getName());

        // Okay... But what happens if the attributes changed?
        // We might have devices of that device type, and they have either:
        //   - attributes that aren't there anymore
        //   - missing attributes that are new
        //
        // So let's fix that!
        if (attributesChanged) {
            // Find all devices that use this type. We only need their identifiers and attributes.
            List<DeviceTable.DeviceAndAttributes> devicesNeedingUpdates
                = server.db().devices().getAllAttribsOfDeviceType(deviceTypeId).await();

            // Maybe we have no devices to update, don't do useless work in that case!
            if (!devicesNeedingUpdates.isEmpty()) {
                // We have some devices to update; begin changing their attributes
                for (var d : devicesNeedingUpdates) {
                    DeviceOperations.fixAttributes(d.attributes(), deviceType, true);
                }

                // Push the changes to the database
                server.db().devices().updateAttributesBatch(devicesNeedingUpdates).await();
            }
        }

        return CompleteDeviceType.fromDeviceType(deviceType);
    }
    // endregion

    // region DELETE /api/device-types/:deviceTypeId | Delete device type
    static final RouteDoc DELETE_DEVICE_TYPE_DOC = new RouteDoc("deleteDeviceType")
        .summary("Delete device type")
        .description("Deletes a device type.")
        .pathParam("deviceTypeId", int.class, "The ID of the device type to delete.")
        .response(204, "The device type was deleted successfully.")
        .response(404, ErrorResponse.class, "Device type not found.")
        .response(422, ErrorResponse.class, "There are still devices with this type. (code: `DEVICES_WITH_TYPE`)");

    void deleteDeviceType(RoutingContext context) {
        int deviceTypeId = readIntPathParam(context, "deviceTypeId");

        // Get the device type
        DeviceType deviceType = server.db().deviceTypes().get(deviceTypeId).await();
        if (deviceType == null) {
            throw new RequestException("Ce type d'appareil n'existe pas.", 404, "NOT_FOUND");
        }

        // See if we have any devices depending on it.
        if (server.db().devices().hasAnyWithDeviceType(deviceType.getId()).await()) {
            throw new RequestException("Il reste encore des appareils avec ce type.", 422, "DEVICES_WITH_TYPE");
        }

        // Delete it!
        server.db().deviceTypes().delete(deviceTypeId).await();
        log.info("Device type deleted with id {}", deviceTypeId);
    }
    // endregion
}