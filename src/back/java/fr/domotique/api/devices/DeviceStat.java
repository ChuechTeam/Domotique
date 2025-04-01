package fr.domotique.api.devices;

import fr.domotique.base.apidocs.*;

@ApiDoc("A group of devices with its calculated attribute value (using sum, avg, etc.)")
public record DeviceStat(
    Object group,
    double value) {
}
