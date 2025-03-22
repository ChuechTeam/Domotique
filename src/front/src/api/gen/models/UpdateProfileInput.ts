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
import type { Gender } from './Gender';
import {
    GenderFromJSON,
    GenderFromJSONTyped,
    GenderToJSON,
    GenderToJSONTyped,
} from './Gender';

/**
 * 
 * @export
 * @interface UpdateProfileInput
 */
export interface UpdateProfileInput {
    /**
     * 
     * @type {string}
     * @memberof UpdateProfileInput
     */
    firstName?: string;
    /**
     * 
     * @type {string}
     * @memberof UpdateProfileInput
     */
    lastName?: string;
    /**
     * 
     * @type {Gender}
     * @memberof UpdateProfileInput
     */
    gender?: Gender;
}



/**
 * Check if a given object implements the UpdateProfileInput interface.
 */
export function instanceOfUpdateProfileInput(value: object): value is UpdateProfileInput {
    return true;
}

export function UpdateProfileInputFromJSON(json: any): UpdateProfileInput {
    return UpdateProfileInputFromJSONTyped(json, false);
}

export function UpdateProfileInputFromJSONTyped(json: any, ignoreDiscriminator: boolean): UpdateProfileInput {
    if (json == null) {
        return json;
    }
    return {
        
        'firstName': json['firstName'] == null ? undefined : json['firstName'],
        'lastName': json['lastName'] == null ? undefined : json['lastName'],
        'gender': json['gender'] == null ? undefined : GenderFromJSON(json['gender']),
    };
}

export function UpdateProfileInputToJSON(json: any): UpdateProfileInput {
    return UpdateProfileInputToJSONTyped(json, false);
}

export function UpdateProfileInputToJSONTyped(value?: UpdateProfileInput | null, ignoreDiscriminator: boolean = false): any {
    if (value == null) {
        return value;
    }

    return {
        
        'firstName': value['firstName'],
        'lastName': value['lastName'],
        'gender': GenderToJSON(value['gender']),
    };
}

