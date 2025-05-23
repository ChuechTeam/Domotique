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
import type { CompleteDevice } from './CompleteDevice';
import {
    CompleteDeviceFromJSON,
    CompleteDeviceFromJSONTyped,
    CompleteDeviceToJSON,
    CompleteDeviceToJSONTyped,
} from './CompleteDevice';

/**
 * 
 * @export
 * @interface DevicesResponse
 */
export interface DevicesResponse {
    /**
     * 
     * @type {Array<CompleteDevice>}
     * @memberof DevicesResponse
     */
    devices: Array<CompleteDevice>;
}

/**
 * Check if a given object implements the DevicesResponse interface.
 */
export function instanceOfDevicesResponse(value: object): value is DevicesResponse {
    if (!('devices' in value) || value['devices'] === undefined) return false;
    return true;
}

export function DevicesResponseFromJSON(json: any): DevicesResponse {
    return DevicesResponseFromJSONTyped(json, false);
}

export function DevicesResponseFromJSONTyped(json: any, ignoreDiscriminator: boolean): DevicesResponse {
    if (json == null) {
        return json;
    }
    return {
        
        'devices': ((json['devices'] as Array<any>).map(CompleteDeviceFromJSON)),
    };
}

export function DevicesResponseToJSON(json: any): DevicesResponse {
    return DevicesResponseToJSONTyped(json, false);
}

export function DevicesResponseToJSONTyped(value?: DevicesResponse | null, ignoreDiscriminator: boolean = false): any {
    if (value == null) {
        return value;
    }

    return {
        
        'devices': ((value['devices'] as Array<any>).map(CompleteDeviceToJSON)),
    };
}

