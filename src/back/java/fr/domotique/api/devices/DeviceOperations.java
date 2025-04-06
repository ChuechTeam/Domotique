package fr.domotique.api.devices;

import fr.domotique.*;
import fr.domotique.data.*;
import org.jetbrains.annotations.*;

import java.util.*;

/// Contains common operations for devices.
public final class DeviceOperations {
    private DeviceOperations() {
    }

    /// Adapts the input attributes to match the device type:
    /// - Removes all attributes that are not in the device type
    /// - Adds default values for all attributes that are missing
    ///
    /// The `attributes` map will be updated.
    public static void fixAttributes(EnumMap<AttributeType, Object> attributes, DeviceType type, boolean addMissing) {
        // Remove all attributes that are NOT present in the device type
        attributes.keySet().removeIf(key -> !type.getAttributes().contains(key));

        if (addMissing) {
            // Add default values for all attributes that are missing
            for (AttributeType attrType : type.getAttributes()) {
                if (!attributes.containsKey(attrType)) {
                    attributes.put(attrType, attrType.defaultValue());
                }
            }
        }
    }

    /// Returns true when a device owned by `ownerId` can be seen by a viewer of id `viewerId` with
    /// role `viewerRole` and level `viewerLevel`.
    public static boolean canSeePersonalAttributes(@Nullable Integer ownerId,
                                                   int viewerId,
                                                   Role viewerRole,
                                                   Level viewerLevel) {
        // No owner; nothing to remove
        if (ownerId == null) {
            return true;
        }

        // Owner; they can see everything
        if (ownerId == viewerId) {
            return true;
        }

        // Intermediate, Expert, caregiver, or admin; they can see this info
        if (viewerLevel.compareTo(Level.INTERMEDIATE) >= 0 || viewerRole == Role.CAREGIVER || viewerRole == Role.ADMIN) {
            return true;
        }

        return false;
    }

    public static boolean canSeePersonalAttributes(@Nullable Integer ownerId, Authenticator auth) {
        var sess = auth.getSession();
        if (sess == null) {
            return false;
        }

        return canSeePersonalAttributes(ownerId, sess.userId(), sess.role(), sess.level());
    }

    public static void nullifyPersonalAttributes(EnumMap<AttributeType, Object> attributes,
                                                 @Nullable Integer ownerId,
                                                 int viewerId,
                                                 Role viewerRole,
                                                 Level viewerLevel) {
        if (canSeePersonalAttributes(ownerId, viewerId, viewerRole, viewerLevel)) {
            return;
        }

        // Set all personal attributes to null then.
        for (var k : attributes.keySet()) {
            if (k.isPersonal()) {
                attributes.put(k, null);
            }
        }
    }

    public static void nullifyPersonalAttributes(EnumMap<AttributeType, Object> attributes,
                                                 @Nullable Integer ownerId,
                                                 Authenticator auth) {
        var sess = auth.getSession();
        if (sess == null) {
            return;
        }

        nullifyPersonalAttributes(attributes, ownerId, sess.userId(), sess.role(), sess.level());
    }
}
