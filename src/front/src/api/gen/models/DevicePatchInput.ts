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
/**
 * Data for PATCH operations on a device.
 * @export
 * @interface DevicePatchInput
 */
export interface DevicePatchInput {
    /**
     * 
     * @type {number}
     * @memberof DevicePatchInput
     */
    energyConsumption: number;
    /**
     * 
     * @type {boolean}
     * @memberof DevicePatchInput
     */
    powered: boolean;
    /**
     * 
     * @type {string}
     * @memberof DevicePatchInput
     */
    name: string;
    /**
     * 
     * @type {string}
     * @memberof DevicePatchInput
     */
    description?: string;
    /**
     * 
     * @type {number}
     * @memberof DevicePatchInput
     */
    typeId: number;
    /**
     * 
     * @type {{ [key: string]: object; }}
     * @memberof DevicePatchInput
     */
    attributes: { [key: string]: object; };
    /**
     * 
     * @type {number}
     * @memberof DevicePatchInput
     */
    userId?: number;
    /**
     * 
     * @type {number}
     * @memberof DevicePatchInput
     */
    roomId?: number;
}

/**
 * Check if a given object implements the DevicePatchInput interface.
 */
export function instanceOfDevicePatchInput(value: object): value is DevicePatchInput {
    if (!('energyConsumption' in value) || value['energyConsumption'] === undefined) return false;
    if (!('powered' in value) || value['powered'] === undefined) return false;
    if (!('name' in value) || value['name'] === undefined) return false;
    if (!('typeId' in value) || value['typeId'] === undefined) return false;
    if (!('attributes' in value) || value['attributes'] === undefined) return false;
    return true;
}

export function DevicePatchInputFromJSON(json: any): DevicePatchInput {
    return DevicePatchInputFromJSONTyped(json, false);
}

export function DevicePatchInputFromJSONTyped(json: any, ignoreDiscriminator: boolean): DevicePatchInput {
    if (json == null) {
        return json;
    }
    return {
        
        'energyConsumption': json['energyConsumption'],
        'powered': json['powered'],
        'name': json['name'],
        'description': json['description'] == null ? undefined : json['description'],
        'typeId': json['typeId'],
        'attributes': json['attributes'],
        'userId': json['userId'] == null ? undefined : json['userId'],
        'roomId': json['roomId'] == null ? undefined : json['roomId'],
    };
}

export function DevicePatchInputToJSON(json: any): DevicePatchInput {
    return DevicePatchInputToJSONTyped(json, false);
}

export function DevicePatchInputToJSONTyped(value?: DevicePatchInput | null, ignoreDiscriminator: boolean = false): any {
    if (value == null) {
        return value;
    }

    return {
        
        'energyConsumption': value['energyConsumption'],
        'powered': value['powered'],
        'name': value['name'],
        'description': value['description'],
        'typeId': value['typeId'],
        'attributes': value['attributes'],
        'userId': value['userId'],
        'roomId': value['roomId'],
    };
}

