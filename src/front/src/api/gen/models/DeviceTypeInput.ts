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
import type { AttributeType } from './AttributeType';
import {
    AttributeTypeFromJSON,
    AttributeTypeFromJSONTyped,
    AttributeTypeToJSON,
    AttributeTypeToJSONTyped,
} from './AttributeType';
import type { DeviceCategory } from './DeviceCategory';
import {
    DeviceCategoryFromJSON,
    DeviceCategoryFromJSONTyped,
    DeviceCategoryToJSON,
    DeviceCategoryToJSONTyped,
} from './DeviceCategory';

/**
 * Data for both INSERT and UPDATE operations on a device type.
 * @export
 * @interface DeviceTypeInput
 */
export interface DeviceTypeInput {
    /**
     * 
     * @type {string}
     * @memberof DeviceTypeInput
     */
    name: string;
    /**
     * 
     * @type {Array<AttributeType>}
     * @memberof DeviceTypeInput
     */
    attributes: Array<AttributeType>;
    /**
     * 
     * @type {DeviceCategory}
     * @memberof DeviceTypeInput
     */
    category: DeviceCategory;
}



/**
 * Check if a given object implements the DeviceTypeInput interface.
 */
export function instanceOfDeviceTypeInput(value: object): value is DeviceTypeInput {
    if (!('name' in value) || value['name'] === undefined) return false;
    if (!('attributes' in value) || value['attributes'] === undefined) return false;
    if (!('category' in value) || value['category'] === undefined) return false;
    return true;
}

export function DeviceTypeInputFromJSON(json: any): DeviceTypeInput {
    return DeviceTypeInputFromJSONTyped(json, false);
}

export function DeviceTypeInputFromJSONTyped(json: any, ignoreDiscriminator: boolean): DeviceTypeInput {
    if (json == null) {
        return json;
    }
    return {
        
        'name': json['name'],
        'attributes': ((json['attributes'] as Array<any>).map(AttributeTypeFromJSON)),
        'category': DeviceCategoryFromJSON(json['category']),
    };
}

export function DeviceTypeInputToJSON(json: any): DeviceTypeInput {
    return DeviceTypeInputToJSONTyped(json, false);
}

export function DeviceTypeInputToJSONTyped(value?: DeviceTypeInput | null, ignoreDiscriminator: boolean = false): any {
    if (value == null) {
        return value;
    }

    return {
        
        'name': value['name'],
        'attributes': ((value['attributes'] as Array<any>).map(AttributeTypeToJSON)),
        'category': DeviceCategoryToJSON(value['category']),
    };
}

