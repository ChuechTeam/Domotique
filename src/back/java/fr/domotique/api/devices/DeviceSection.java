package fr.domotique.api.devices;

import com.fasterxml.jackson.annotation.*;
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
import org.openapitools.jackson.nullable.*;
import org.slf4j.*;

import java.time.*;
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
        deviceRoutes.post("/stats").respond(this::getDeviceStats).putMetadata(RouteDoc.KEY, GET_DEVICE_STATS_DOC);

        // Routes with parameters come last
        deviceRoutes.get("/:deviceId").respond(this::getDeviceById).putMetadata(RouteDoc.KEY, GET_DEVICE_DOC);
        deviceRoutes.patch("/:deviceId").respond(vt(this::patchDevice)).putMetadata(RouteDoc.KEY, PATCH_DEVICE_DOC);
        deviceRoutes.delete("/:deviceId").respond(vt(this::deleteDevice)).putMetadata(RouteDoc.KEY, DELETE_DEVICE_DOC);

        deviceRoutes.get("/:deviceId/report").respond(vt(this::genReport)).putMetadata(RouteDoc.KEY, GEN_REPORT_DOC);
    }

    // region GET /api/devices | Get all devices
    static final RouteDoc GET_DEVICES_DOC = new RouteDoc("getDevices")
        .summary("Get devices")
        .description("Gets all devices from the database matching the given filters. All query parameters are optional.")
        .optionalQueryParam("ids", int[].class, "The IDs of the devices to get. If set, ignores all other parameters.")
        .optionalQueryParam("name", String.class, "The name of the device to search for.")
        .optionalQueryParam("typeId", int.class, "Filters the devices by this type.")
        .optionalQueryParam("roomId", int.class, "Filters the devices by this room.")
        .optionalQueryParam("userId", int.class, "Filters the devices by this owner.")
        .optionalQueryParam("powered", boolean.class, "Filters the devices by their power status: `true`, or `false`.")
        .optionalQueryParam("category", DeviceCategory.class, "Filters the devices by their category.")
        .response(200, DevicesResponse.class, "The list of all devices.");

    record DevicesResponse(List<CompleteDevice> devices) {}

    Future<DevicesResponse> getAll(RoutingContext context) {
        Authenticator authenticator = Authenticator.get(context);

        List<Integer> ids = readIntListFromQueryParams(context, "ids");
        if (!ids.isEmpty()) {
            // If we have IDs, we ignore all other parameters
            return server.db().devices().getCompleteAll(ids).map(DevicesResponse::new);
        }

        String name = Sanitize.string(context.queryParams().get("name"));
        Integer typeId = readIntOrNull(context.queryParams().get("typeId"));
        Integer roomId = readIntOrNull(context.queryParams().get("roomId"));
        Integer userId = readIntOrNull(context.queryParams().get("userId"));
        Boolean powered = readBooleanOrNull(context.queryParams().get("powered"));

        String categoryStr = context.queryParams().get("category");
        DeviceCategory category = null;
        if (categoryStr != null) {
            try {
                category = DeviceCategory.valueOf(categoryStr);
            } catch (IllegalArgumentException e) {
                throw new RequestException("Catégorie inconnue.", 422, "INVALID_CATEGORY");
            }
        }

        var query = new DeviceTable.CompleteQuery(name, typeId, roomId, userId, powered, category);

        return server.db().devices().queryComplete(query)
            .map(devices -> {
                // Remove personal data from the devices
                for (CompleteDevice dev : devices) {
                    nullifyPersonalData(dev, authenticator);
                }

                return devices;
            })
            .map(DevicesResponse::new);
    }
    // endregion

    // region GET /api/devices/stats | Get device stats
    static final RouteDoc GET_DEVICE_STATS_DOC = new RouteDoc("getDeviceStats")
        .summary("Get device stats")
        .description("Gets the stats for devices.")
        .requestBody(DeviceStatsQuery.class)
        .response(200, DeviceStats.class, "The device stats.", new DeviceStats(
            List.of(new DeviceStat(1, 80), new DeviceStat(8, 41))
        ))
        .response(204, "Device not found.")
        .response(403, ErrorResponse.class, "You don't have permission to access this resource.")
        .response(422, ErrorResponse.class, "Invalid query.");

    record DeviceStats(List<DeviceStat> stats) {}

    Future<DeviceStats> getDeviceStats(RoutingContext context) {
        Authenticator auth = Authenticator.get(context);

        // TODO: Make it send full entities for room, user, and device.
        var body = readBody(context, DeviceStatsQuery.class);
        if (!body.validQuery()) {
            throw new RequestException("Invalid query.", 422, "INVALID_QUERY");
        }

        // Personal attributes can't be queried. TODO: Instead, add a ownerId = thisUserId to the query.
        if (body.attribute().isPersonal()
            && !DeviceOperations.canSeePersonalAttributes(0, auth)) {
            throw new RequestException("Vous n'avez pas le niveau requis pour voir cet attribut.", 403, "PERSONAL_ATTRIBUTE");
        }

        return server.db().devices().queryStats(body)
            .map(DeviceStats::new);
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
        Authenticator auth = Authenticator.get(context);
        int deviceId = readIntPathParam(context, "deviceId");
        return server.db().devices().getComplete(deviceId).map(d -> nullifyPersonalData(d, auth));
    }
    // endregion

    // todo: less code duplication with DevicePathInput for both + patchRequestBody

    @ApiDoc("Data for both POST and PUT operations on a device.")
    record DeviceInput(
        String name,
        @Nullable String description,
        int typeId,
        @ApiDoc(optional = true) Integer roomId,
        @ApiDoc(optional = true) Integer userId,
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
                DeviceValidation.name(block, name);
                DeviceValidation.description(block, description);
                DeviceValidation.energyConsumption(block, energyConsumption);
                DeviceValidation.attributes(block, attributes);
            }
        }
    }

    @ApiDoc(value = "Data for PATCH operations on a device.", optional = true)
    record DevicePatchInput(
        String name, // required
        JsonNullable<String> description,
        Integer typeId, // required
        JsonNullable<Integer> roomId,
        JsonNullable<Integer> userId,
        EnumMap<AttributeType, Object> attributes,
        Boolean powered, // required
        Double energyConsumption, // required
        JsonNullable<Integer> deletionRequestedById
    ) {
        public DevicePatchInput {
            name = Sanitize.string(name);
            description = mapNullable(description, Sanitize::string);

            // Ensure the attributes map is never null, it will always have a default value of an empty map
            if (attributes == null) {
                attributes = new EnumMap<>(AttributeType.class);
            }
        }

        /// Runs validation for this input.
        public void validate() {
            try (var block = Validation.start()) {
                if (name != null) DeviceValidation.name(block, name);
                DeviceValidation.description(block, description.orElse(null));
                if (energyConsumption != null) DeviceValidation.energyConsumption(block, energyConsumption);
                DeviceValidation.attributes(block, attributes);
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
            - The room was not found (code: `ROOM_NOT_FOUND`)
            - The owner user was not found (code: `USER_NOT_FOUND`)""");

    static final RouteDoc CREATE_DEVICE_DOC = new RouteDoc("createDevice")
        .summary("Create device")
        .description("Creates a new device.")
        .requestBody(DeviceInput.class, new DeviceInput(
            "My Smart Watch",
            "Description of my device",
            1, // typeId
            1, // roomId
            null, // userId
            new EnumMap<>(AttributeType.class),
            true, // powered
            5.5 // energyConsumption
        ))
        .response(201, CompleteDevice.class, "The device was created successfully.")
        .response(404, ErrorResponse.class, "Device not found.")
        .response(CREATE_OR_UPDATE_ERR);

    CompleteDevice createDevice(RoutingContext context) {
        Authenticator auth = Authenticator.get(context);
        DeviceInput input = readBody(context, DeviceInput.class);

        // Validate data
        input.validate();

        // Find the device type
        DeviceType deviceType = server.db().deviceTypes().get(input.typeId).await();
        if (deviceType == null) {
            throw new RequestException("Type d'appareil introuvable.", 404, "DEVICE_TYPE_NOT_FOUND");
        }

        // Add any missing attributes, remove those that are not in the device type
        DeviceOperations.fixAttributes(input.attributes, deviceType, true);

        // Create the device
        Device device = new Device(
            0,
            input.name,
            input.description,
            input.typeId,
            input.roomId,
            input.userId,
            input.attributes,
            input.powered,
            input.energyConsumption,
            null
        );

        try {
            server.db().devices().insert(device).await();
            log.info("Device created with id {} and name {}", device.getId(), device.getName());

            String status;
            if (device.isPowered()) {
                status = "POWER_ON";
            } else {
                status = "POWER_OFF";
            }
            server.db().powerLogs().insert(
                new PowerLog(device.getId(), status, Instant.now(), device.getEnergyConsumption())
            ).await();

            // Log the changes
            server.db().actionLogs().insert(new ActionLog(
                auth.getUserId(),
                device.getId(),
                ActionLogTarget.DEVICE,
                ActionLogOperation.CREATE
            )).await();

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

    // region PATCH /api/devices/:deviceId | Patch device
    static final RouteDoc PATCH_DEVICE_DOC = new RouteDoc("patchDevice")
        .summary("Patch device")
        .description("Patches an existing device.")
        .pathParam("deviceId", int.class, "The ID of the device to patch.")
        .requestBody(DevicePatchInput.class, new DevicePatchInput(
            null,
            JsonNullable.of("Updated description"),
            null,
            JsonNullable.of(1),
            JsonNullable.of(1),
            new EnumMap<>(AttributeType.class),
            null,
            null,
            JsonNullable.undefined()
        ))
        .response(200, CompleteDevice.class, "The device was patched successfully.")
        .response(404, ErrorResponse.class, "Device not found.")
        .response(CREATE_OR_UPDATE_ERR);

    CompleteDevice patchDevice(RoutingContext context) {
        Authenticator auth = Authenticator.get(context);

        int deviceId = readIntPathParam(context, "deviceId");
        DevicePatchInput input = readBody(context, DevicePatchInput.class);

        // Validate device input
        input.validate();

        // Get the device
        Device device = server.db().devices().get(deviceId).await();
        if (device == null) {
            throw new RequestException("Appareil introuvable.", 404, "DEVICE_NOT_FOUND");
        }

        // Find the device type
        int typeId = input.typeId != null ? input.typeId : device.getTypeId();
        DeviceType deviceType = server.db().deviceTypes().get(typeId).await();
        if (deviceType == null) {
            throw new RequestException("Type d'appareil introuvable.", 404, "DEVICE_TYPE_NOT_FOUND");
        }

        // Make sure the new deletionRequestedById is valid and that we have the right to set it
        if (input.deletionRequestedById.isPresent()) {
            Integer delId = input.deletionRequestedById.get();
            // Don't check when values are the same.
            if (!Objects.equals(delId, device.getDeletionRequestedById())) {
                if (delId == null && !auth.hasLevel(Level.ADVANCED)) {
                    throw new RequestException("Vous n'avez pas le niveau requis pour ignorer une demande de suppression.",
                        403, "CANNOT_IGNORE_DELETION_REQUEST");
                } else if (delId != null && delId != auth.getUserId()) {
                    throw new RequestException("Impossible d'ajouter une demande de suppression pour un autre utilisateur.",
                        422, "CANNOT_IMPERSONATE_DELETION_REQUEST");
                }
            }
        }

        // See if our device turned off or on.
        boolean devicePowerChanged = input.powered != null && input.powered != device.isPowered();
        // See if we cleared or added a deletion request
        boolean deletionRequestChanged = input.deletionRequestedById.isPresent()
                                         && !Objects.equals(input.deletionRequestedById.get(), device.getDeletionRequestedById());
        boolean energyConsumptionChanged = input.energyConsumption != null
                                           && input.energyConsumption != device.getEnergyConsumption();

        // Update device properties
        if (input.name != null) device.setName(input.name);
        input.description.ifPresent(device::setDescription);
        if (input.typeId != null) device.setTypeId(input.typeId);
        input.roomId.ifPresent(device::setRoomId);
        input.userId.ifPresent(device::setUserId);

        input.attributes.forEach((k, v) -> device.getAttributes().put(k, v));
        DeviceOperations.fixAttributes(device.getAttributes(), deviceType, true);

        if (input.powered != null) device.setPowered(input.powered);
        if (input.energyConsumption != null) device.setEnergyConsumption(input.energyConsumption);
        input.deletionRequestedById.ifPresent(device::setDeletionRequestedById);

        try {
            // Update device on the database
            server.db().devices().update(device).await();
            log.info("Device patched with id {} and name {}", device.getId(), device.getName());

            if (devicePowerChanged || energyConsumptionChanged) {
                String status;
                if (device.isPowered()) {
                    status = "POWER_ON";
                } else {
                    status = "POWER_OFF";
                }
                server.db().powerLogs().insert(
                    new PowerLog(device.getId(), status, Instant.now(), device.getEnergyConsumption())
                ).await();
            }

            // Log the changes
            var flags = EnumSet.noneOf(ActionLogFlags.class);
            if (devicePowerChanged) {
                flags.add(input.powered ? ActionLogFlags.POWER_ON : ActionLogFlags.POWER_OFF);
            }
            if (deletionRequestChanged) {
                flags.add(input.deletionRequestedById.get() != null ? ActionLogFlags.DELETE_REQUESTED : ActionLogFlags.DELETE_REQUEST_DELETED);
            }
            server.db().actionLogs().insert(new ActionLog(auth.getUserId(), deviceId,
                ActionLogTarget.DEVICE,
                ActionLogOperation.UPDATE,
                flags
            )).await();

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
        // Must be an expert user to delete a device
        Authenticator auth = Authenticator.get(context);
        auth.requireAuth(Level.EXPERT);

        int deviceId = readIntPathParam(context, "deviceId");

        // Get the device
        Device device = server.db().devices().get(deviceId).await();
        if (device == null) {
            throw new RequestException("Cet appareil n'existe pas.", 404, "NOT_FOUND");
        }

        // Delete device and log it!
        server.db().devices().delete(deviceId).await();
        server.db().actionLogs().insert(new ActionLog(auth.getUserId(), deviceId, ActionLogTarget.DEVICE, ActionLogOperation.DELETE)).await();
        log.info("Device deleted with id {}", deviceId);
    }
    // endregion

    static final RouteDoc GEN_REPORT_DOC = new RouteDoc("genReport")
        .summary("Generate a report for a device")
        .description("Generates a report for a device.")
        .pathParam("deviceId", int.class, "The ID of the device to generate the report for.")
        .response(200, Object.class, "The report was generated successfully.")
        .response(404, ErrorResponse.class, "Device not found.");

    Object genReport(RoutingContext context) {
        int deviceId = readIntPathParam(context, "deviceId");


        Device device = server.db().devices().get(deviceId).await();
        if (device == null) {
            throw new RequestException("Appareil introuvable.", 404, "DEVICE_NOT_FOUND");
        }

        Map<String, Object> report = new HashMap<>();
        report.put("deviceId", device.getId());
        report.put("name", device.getName());
        report.put("powered", device.isPowered());
        report.put("energyConsumption", device.getEnergyConsumption());

        String efficiencyStatus = device.getEnergyConsumption() > 200 ? "Inefficient" : "Normal";
        boolean needsMaintenance = !device.isPowered() && device.getEnergyConsumption() > 50;

        report.put("efficiencyStatus", efficiencyStatus);
        report.put("needsMaintenance", needsMaintenance);

        return report;
    }

    /// Throw an API error when a foreign key constraint fails for rooms or device types.
    private RuntimeException missingRoomOrTypeErr(ForeignException ex) {
        if (ex.getMessage().contains(DeviceTable.ROOM_FK)) {
            throw new RequestException("La pièce n'existe pas.", 422, "ROOM_NOT_FOUND");
        } else if (ex.getMessage().contains(DeviceTable.TYPE_FK)) {
            throw new RequestException("Le type d'appareil n'existe pas.", 422, "DEVICE_TYPE_NOT_FOUND");
        } else if (ex.getMessage().contains(DeviceTable.USER_FK)) {
            throw new RequestException("Le propriétaire n'existe pas.", 422, "OWNER_NOT_FOUND");
        } else if (ex.getMessage().contains(DeviceTable.DELETION_REQ_USER_FK)) {
            throw new RequestException("Le demandeur de la suppression de l'appareil n'existe pas.", 422, "DELETION_REQUEST_USER_NOT_FOUND");
        } else {
            throw new IllegalStateException("Unknown foreign key error: " + ex.getMessage(), ex);
        }
    }

    private @Nullable CompleteDevice nullifyPersonalData(@Nullable CompleteDevice dev, Authenticator a) {
        if (dev == null) {
            return null;
        }
        DeviceOperations.nullifyPersonalAttributes(dev.attributes(), dev.ownerId(), a);
        return dev;
    }
}
