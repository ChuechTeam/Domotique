package fr.domotique.api.devices;

import fr.domotique.*;
import fr.domotique.base.*;
import fr.domotique.base.Validation;
import fr.domotique.base.apidocs.*;
import fr.domotique.base.data.*;
import fr.domotique.data.*;
import io.vertx.core.Future;
import io.vertx.core.http.*;
import io.vertx.ext.web.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

import java.util.*;

/// All API endpoints to access device data
public class DeviceSection extends Section {
    /// Creates a new DeviceSection with the given server.
    public DeviceSection(Server server) {
        super(server);
    }

    // All routes of this section will begin with /api/devices
    static final String PATH_PREFIX = "/api/devices*";

    // Logger for this section
    static final Logger log = LoggerFactory.getLogger(DeviceSection.class);

    @Override
    public void register(Router router) {
        // Create a sub-router for all device routes
        var deviceRoutes = newSubRouter(router, PATH_PREFIX)
            .putMetadata(RouteDoc.KEY, new RouteDoc().tag("Devices")
            .response(401, ErrorResponse.class, "You are not logged in.")
            .response(403, ErrorResponse.class, "You don't have permission to access this resource."));

        // When:
        // - A user requests data, they must be AT LEAST a BEGINNER (= confirmed email)
        // - A user modifies data, they must be AT LEAST an ADVANCED user
        deviceRoutes.route().handler(ctx -> {
            Authenticator auth = Authenticator.get(ctx);

            if (ctx.request().method() == HttpMethod.GET) {
                auth.requireAuth(Level.BEGINNER);
            } else {
                auth.requireAuth(Level.ADVANCED);
            }

            // Don't stop there! Continue the request.
            ctx.next();
        });

        // Register all device-related endpoints
        deviceRoutes.get("/").respond(this::getAll).putMetadata(RouteDoc.KEY, GET_DEVICES_DOC);
        deviceRoutes.post("/").respond(vt(this::createDevice)).putMetadata(RouteDoc.KEY, CREATE_DEVICE_DOC);

        // Routes with parameters come last
        deviceRoutes.get("/:deviceId").respond(this::getDeviceById).putMetadata(RouteDoc.KEY, GET_DEVICE_DOC);
        deviceRoutes.put("/:deviceId").respond(vt(this::updateDevice)).putMetadata(RouteDoc.KEY, UPDATE_DEVICE_DOC);
        deviceRoutes.delete("/:deviceId").respond(vt(this::deleteDevice)).putMetadata(RouteDoc.KEY, DELETE_DEVICE_DOC);
    }

    // region GET /api/devices | Get all devices
    static final RouteDoc GET_DEVICES_DOC = new RouteDoc("getDevices")
        .summary("Get devices")
        .description("Gets all devices from the database.")
        .response(200, DevicesResponse.class, "The list of all devices.");

    record DevicesResponse(List<CompleteDevice> devices) {}

    Future<DevicesResponse> getAll(RoutingContext context) {
        return server.db().devices().getCompleteAll()
            .map(DevicesResponse::new);
    }
    // endregion

    // region GET /api/devices/:deviceId | Get device by ID
    static final RouteDoc GET_DEVICE_DOC = new RouteDoc("getDeviceById")
        .summary("Get device by ID")
        .description("Gets a device by its ID.")
        .pathParam("deviceId", int.class, "The ID of the device.")
        .response(200, CompleteDevice.class, "The device data.")
        .response(204, "Device not found.");

    Future<CompleteDevice> getDeviceById(RoutingContext context) {
        int deviceId = readIntPathParam(context, "deviceId");
        return server.db().devices().getComplete(deviceId);
    }
    // endregion

    @ApiDoc("Data for both INSERT and UPDATE operations on a device.")
    record DeviceInput(
        String name,
        @Nullable String description,
        int typeId,
        int roomId,
        EnumMap<AttributeType, Object> attributes,
        boolean powered,
        double energyConsumption
    ) {
        public DeviceInput {
            name = Sanitize.string(name);
            description = Sanitize.string(description);

            // Ensure the attributes map is never null, it will always have a default value of an empty map
            if (attributes == null) {
                attributes = new EnumMap<>(AttributeType.class);
            }
        }

        /// Runs validation for this input.
        public void validate() {
            try (var block = Validation.start()) {
                Validation.lengthIn(block, "name", name, 1, 128,
                    "Le nom est vide.",
                    "Le nom est trop long.");

                if (description != null) {
                    Validation.lengthIn(block, "description", description, 0, 16000,
                        "La description est trop longue.");
                }

                if (energyConsumption < 0) {
                    block.addError("energyConsumption", "La consommation d'énergie ne peut pas être négative.");
                }
            }
        }
    }

    // region POST /api/devices | Create device

    // Error common to both create and update operations
    static final ResponseDoc CREATE_OR_UPDATE_ERR =
        new ResponseDoc().content(ErrorResponse.class).status(422).description("""
            Either:
            - There are some validation errors (code: `VALIDATION_ERROR`)
            - The device type was not found (code: `DEVICE_TYPE_NOT_FOUND`)
            - The room was not found (code: `ROOM_NOT_FOUND`)""");

    static final RouteDoc CREATE_DEVICE_DOC = new RouteDoc("createDevice")
        .summary("Create device")
        .description("Creates a new device.")
        .requestBody(DeviceInput.class, new DeviceInput(
            "My Smart Watch",
            "Description of my device",
            1, // typeId
            1, // roomId
            new EnumMap<>(AttributeType.class),
            true, // powered
            5.5 // energyConsumption
        ))
        .response(201, CompleteDevice.class, "The device was created successfully.")
        .response(404, ErrorResponse.class, "Device not found.")
        .response(CREATE_OR_UPDATE_ERR);

    CompleteDevice createDevice(RoutingContext context) {
        DeviceInput input = readBody(context, DeviceInput.class);

        // Validate data
        input.validate();

        // Find the device type
        DeviceType deviceType = server.db().deviceTypes().get(input.typeId).await();
        if (deviceType == null) {
            throw new RequestException("Type d'appareil introuvable.", 404, "DEVICE_TYPE_NOT_FOUND");
        }

        // Add any missing attributes, remove those that are not in the device type
        DeviceOperations.fixAttributes(input.attributes, deviceType);

        // Create the device
        Device device = new Device(
            0,
            input.name,
            input.description,
            input.typeId,
            input.roomId,
            input.attributes,
            input.powered,
            input.energyConsumption
        );

        try {
            server.db().devices().insert(device).await();
            log.info("Device created with id {} and name {}", device.getId(), device.getName());

            // TODO: Add an entry in the database to log devices turning off/on

            // Get the complete device with all the related data
            CompleteDevice completeDevice = server.db().devices().getComplete(device.getId()).await();
            context.response().setStatusCode(201);
            return completeDevice;
        } catch (ForeignException e) {
            // In case of missing user/room
            throw missingRoomOrTypeErr(e);
        }
    }
    // endregion

    // region POST /api/devices/:deviceId | Update device
    static final RouteDoc UPDATE_DEVICE_DOC = new RouteDoc("updateDevice")
        .summary("Update device")
        .description("Updates an existing device.")
        .pathParam("deviceId", int.class, "The ID of the device to update.")
        .requestBody(DeviceInput.class, new DeviceInput(
            "Updated Smart Watch",
            "Updated description",
            1, // typeId
            1, // roomId
            new EnumMap<>(AttributeType.class),
            false, // powered
            3.2 // energyConsumption
        ))
        .response(200, CompleteDevice.class, "The device was updated successfully.")
        .response(404, ErrorResponse.class, "Device not found.")
        .response(CREATE_OR_UPDATE_ERR);

    CompleteDevice updateDevice(RoutingContext context) {
        int deviceId = readIntPathParam(context, "deviceId");
        DeviceInput input = readBody(context, DeviceInput.class);

        // Validate device input
        input.validate();

        // Get the device
        Device device = server.db().devices().get(deviceId).await();
        if (device == null) {
            throw new RequestException("Appareil introuvable.", 404, "DEVICE_NOT_FOUND");
        }

        // Find the device type
        DeviceType deviceType = server.db().deviceTypes().get(input.typeId).await();
        if (deviceType == null) {
            throw new RequestException("Type d'appareil introuvable.", 404, "DEVICE_TYPE_NOT_FOUND");
        }

        // Add any missing attributes, remove those that are not in the device type
        DeviceOperations.fixAttributes(input.attributes, deviceType);

        // See if our device turned off or on.
        boolean devicePowerChanged = input.powered != device.isPowered();

        // Update device properties
        device.setName(input.name);
        device.setDescription(input.description);
        device.setTypeId(input.typeId);
        device.setRoomId(input.roomId);
        device.setAttributes(input.attributes);
        device.setPowered(input.powered);
        device.setEnergyConsumption(input.energyConsumption);

        try {
            // Update device on the database
            server.db().devices().update(device).await();
            log.info("Device updated with id {} and name {}", device.getId(), device.getName());

            if (devicePowerChanged) {
                // TODO: Add an entry in the database to log devices turning off/on
            }

            // Get the complete device with all the related data
            return server.db().devices().getComplete(device.getId()).await();
        } catch (ForeignException e) {
            // In case of missing user/room
            throw missingRoomOrTypeErr(e);
        }
    }
    // endregion

    // region DELETE /api/devices/:deviceId | Delete device
    static final RouteDoc DELETE_DEVICE_DOC = new RouteDoc("deleteDevice")
        .summary("Delete device")
        .description("Deletes a device.")
        .pathParam("deviceId", int.class, "The ID of the device to delete.")
        .response(204, "The device was deleted successfully.")
        .response(404, ErrorResponse.class, "Device not found.");

    void deleteDevice(RoutingContext context) {
        int deviceId = readIntPathParam(context, "deviceId");

        // Get the device
        Device device = server.db().devices().get(deviceId).await();
        if (device == null) {
            throw new RequestException("Cet appareil n'existe pas.", 404, "NOT_FOUND");
        }

        // Delete device
        server.db().devices().delete(deviceId).await();
        log.info("Device deleted with id {}", deviceId);
    }
    // endregion

    /// Throw an API error when a foreign key constraint fails for rooms or device types.
    private RuntimeException missingRoomOrTypeErr(ForeignException ex) {
        if (ex.getMessage().contains(DeviceTable.ROOM_FK)) {
            throw new RequestException("La pièce n'existe pas.", 422, "ROOM_NOT_FOUND");
        } else if (ex.getMessage().contains(DeviceTable.TYPE_FK)) {
            throw new RequestException("Le type d'appareil n'existe pas.", 422, "DEVICE_TYPE_NOT_FOUND");
        } else {
            throw new IllegalStateException("Unknown foreign key error: " + ex.getMessage(), ex);
        }
    }
}