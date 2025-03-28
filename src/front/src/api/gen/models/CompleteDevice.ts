/* tslint:disable */
/* eslint-disable */
/**
 * Domotique API
 * API documentation for the Domotique website.
 *
 * The version of the OpenAPI document: 0.1.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

import { mapValues } from '../runtime';
import type { CompleteDeviceType } from './CompleteDeviceType';
import {
    CompleteDeviceTypeFromJSON,
    CompleteDeviceTypeFromJSONTyped,
    CompleteDeviceTypeToJSON,
    CompleteDeviceTypeToJSONTyped,
} from './CompleteDeviceType';
import type { UserProfile } from './UserProfile';
import {
    UserProfileFromJSON,
    UserProfileFromJSONTyped,
    UserProfileToJSON,
    UserProfileToJSONTyped,
} from './UserProfile';
import type { CompleteRoom } from './CompleteRoom';
import {
    CompleteRoomFromJSON,
    CompleteRoomFromJSONTyped,
    CompleteRoomToJSON,
    CompleteRoomToJSONTyped,
} from './CompleteRoom';

/**
 * A device including its type, and the room its in.
 * @export
 * @interface CompleteDevice
 */
export interface CompleteDevice {
    /**
     * 
     * @type {UserProfile}
     * @memberof CompleteDevice
     */
    owner?: UserProfile;
    /**
     * 
     * @type {number}
     * @memberof CompleteDevice
     */
    energyConsumption: number;
    /**
     * 
     * @type {boolean}
     * @memberof CompleteDevice
     */
    powered: boolean;
    /**
     * 
     * @type {string}
     * @memberof CompleteDevice
     */
    name: string;
    /**
     * 
     * @type {string}
     * @memberof CompleteDevice
     */
    description?: string;
    /**
     * 
     * @type {{ [key: string]: object; }}
     * @memberof CompleteDevice
     */
    attributes: { [key: string]: object; };
    /**
     * 
     * @type {number}
     * @memberof CompleteDevice
     */
    id: number;
    /**
     * 
     * @type {CompleteDeviceType}
     * @memberof CompleteDevice
     */
    type: CompleteDeviceType;
    /**
     * 
     * @type {CompleteRoom}
     * @memberof CompleteDevice
     */
    room?: CompleteRoom;
}

/**
 * Check if a given object implements the CompleteDevice interface.
 */
export function instanceOfCompleteDevice(value: object): value is CompleteDevice {
    if (!('energyConsumption' in value) || value['energyConsumption'] === undefined) return false;
    if (!('powered' in value) || value['powered'] === undefined) return false;
    if (!('name' in value) || value['name'] === undefined) return false;
    if (!('attributes' in value) || value['attributes'] === undefined) return false;
    if (!('id' in value) || value['id'] === undefined) return false;
    if (!('type' in value) || value['type'] === undefined) return false;
    return true;
}

export function CompleteDeviceFromJSON(json: any): CompleteDevice {
    return CompleteDeviceFromJSONTyped(json, false);
}

export function CompleteDeviceFromJSONTyped(json: any, ignoreDiscriminator: boolean): CompleteDevice {
    if (json == null) {
        return json;
    }
    return {
        
        'owner': json['owner'] == null ? undefined : UserProfileFromJSON(json['owner']),
        'energyConsumption': json['energyConsumption'],
        'powered': json['powered'],
        'name': json['name'],
        'description': json['description'] == null ? undefined : json['description'],
        'attributes': json['attributes'],
        'id': json['id'],
        'type': CompleteDeviceTypeFromJSON(json['type']),
        'room': json['room'] == null ? undefined : CompleteRoomFromJSON(json['room']),
    };
}

export function CompleteDeviceToJSON(json: any): CompleteDevice {
    return CompleteDeviceToJSONTyped(json, false);
}

export function CompleteDeviceToJSONTyped(value?: CompleteDevice | null, ignoreDiscriminator: boolean = false): any {
    if (value == null) {
        return value;
    }

    return {
        
        'owner': UserProfileToJSON(value['owner']),
        'energyConsumption': value['energyConsumption'],
        'powered': value['powered'],
        'name': value['name'],
        'description': value['description'],
        'attributes': value['attributes'],
        'id': value['id'],
        'type': CompleteDeviceTypeToJSON(value['type']),
        'room': CompleteRoomToJSON(value['room']),
    };
}

