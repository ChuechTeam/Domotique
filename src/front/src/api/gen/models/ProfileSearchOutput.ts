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
import type { UserProfile } from './UserProfile';
import {
    UserProfileFromJSON,
    UserProfileFromJSONTyped,
    UserProfileToJSON,
    UserProfileToJSONTyped,
} from './UserProfile';

/**
 * 
 * @export
 * @interface ProfileSearchOutput
 */
export interface ProfileSearchOutput {
    /**
     * 
     * @type {Array<UserProfile>}
     * @memberof ProfileSearchOutput
     */
    profiles?: Array<UserProfile>;
}

/**
 * Check if a given object implements the ProfileSearchOutput interface.
 */
export function instanceOfProfileSearchOutput(value: object): value is ProfileSearchOutput {
    return true;
}

export function ProfileSearchOutputFromJSON(json: any): ProfileSearchOutput {
    return ProfileSearchOutputFromJSONTyped(json, false);
}

export function ProfileSearchOutputFromJSONTyped(json: any, ignoreDiscriminator: boolean): ProfileSearchOutput {
    if (json == null) {
        return json;
    }
    return {
        
        'profiles': json['profiles'] == null ? undefined : ((json['profiles'] as Array<any>).map(UserProfileFromJSON)),
    };
}

export function ProfileSearchOutputToJSON(json: any): ProfileSearchOutput {
    return ProfileSearchOutputToJSONTyped(json, false);
}

export function ProfileSearchOutputToJSONTyped(value?: ProfileSearchOutput | null, ignoreDiscriminator: boolean = false): any {
    if (value == null) {
        return value;
    }

    return {
        
        'profiles': value['profiles'] == null ? undefined : ((value['profiles'] as Array<any>).map(UserProfileToJSON)),
    };
}

