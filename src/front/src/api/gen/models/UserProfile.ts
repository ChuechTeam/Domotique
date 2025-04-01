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
import type { Role } from './Role';
import {
    RoleFromJSON,
    RoleFromJSONTyped,
    RoleToJSON,
    RoleToJSONTyped,
} from './Role';
import type { Level } from './Level';
import {
    LevelFromJSON,
    LevelFromJSONTyped,
    LevelToJSON,
    LevelToJSONTyped,
} from './Level';
import type { Gender } from './Gender';
import {
    GenderFromJSON,
    GenderFromJSONTyped,
    GenderToJSON,
    GenderToJSONTyped,
} from './Gender';

/**
 * Public information about a user.
 * @export
 * @interface UserProfile
 */
export interface UserProfile {
    /**
     * The first name of the user.
     * @type {string}
     * @memberof UserProfile
     */
    firstName: string;
    /**
     * The last name of the user.
     * @type {string}
     * @memberof UserProfile
     */
    lastName: string;
    /**
     * 
     * @type {Role}
     * @memberof UserProfile
     */
    role: Role;
    /**
     * 
     * @type {Gender}
     * @memberof UserProfile
     */
    gender: Gender;
    /**
     * 
     * @type {Level}
     * @memberof UserProfile
     */
    level: Level;
    /**
     * The unique identifier of the user. A value of 0 is invalid.
     * @type {number}
     * @memberof UserProfile
     */
    id: number;
    /**
     * The number of points this user has accumulated.
     * @type {number}
     * @memberof UserProfile
     */
    points: number;
}



/**
 * Check if a given object implements the UserProfile interface.
 */
export function instanceOfUserProfile(value: object): value is UserProfile {
    if (!('firstName' in value) || value['firstName'] === undefined) return false;
    if (!('lastName' in value) || value['lastName'] === undefined) return false;
    if (!('role' in value) || value['role'] === undefined) return false;
    if (!('gender' in value) || value['gender'] === undefined) return false;
    if (!('level' in value) || value['level'] === undefined) return false;
    if (!('id' in value) || value['id'] === undefined) return false;
    if (!('points' in value) || value['points'] === undefined) return false;
    return true;
}

export function UserProfileFromJSON(json: any): UserProfile {
    return UserProfileFromJSONTyped(json, false);
}

export function UserProfileFromJSONTyped(json: any, ignoreDiscriminator: boolean): UserProfile {
    if (json == null) {
        return json;
    }
    return {
        
        'firstName': json['firstName'],
        'lastName': json['lastName'],
        'role': RoleFromJSON(json['role']),
        'gender': GenderFromJSON(json['gender']),
        'level': LevelFromJSON(json['level']),
        'id': json['id'],
        'points': json['points'],
    };
}

export function UserProfileToJSON(json: any): UserProfile {
    return UserProfileToJSONTyped(json, false);
}

export function UserProfileToJSONTyped(value?: UserProfile | null, ignoreDiscriminator: boolean = false): any {
    if (value == null) {
        return value;
    }

    return {
        
        'firstName': value['firstName'],
        'lastName': value['lastName'],
        'role': RoleToJSON(value['role']),
        'gender': GenderToJSON(value['gender']),
        'level': LevelToJSON(value['level']),
        'id': value['id'],
        'points': value['points'],
    };
}

