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
 * 
 * @export
 * @interface LoginLog
 */
export interface LoginLog {
    /**
     * 
     * @type {number}
     * @memberof LoginLog
     */
    id: number;
    /**
     * 
     * @type {Date}
     * @memberof LoginLog
     */
    time: Date;
    /**
     * 
     * @type {number}
     * @memberof LoginLog
     */
    userId: number;
}

/**
 * Check if a given object implements the LoginLog interface.
 */
export function instanceOfLoginLog(value: object): value is LoginLog {
    if (!('id' in value) || value['id'] === undefined) return false;
    if (!('time' in value) || value['time'] === undefined) return false;
    if (!('userId' in value) || value['userId'] === undefined) return false;
    return true;
}

export function LoginLogFromJSON(json: any): LoginLog {
    return LoginLogFromJSONTyped(json, false);
}

export function LoginLogFromJSONTyped(json: any, ignoreDiscriminator: boolean): LoginLog {
    if (json == null) {
        return json;
    }
    return {
        
        'id': json['id'],
        'time': (new Date(json['time'])),
        'userId': json['userId'],
    };
}

export function LoginLogToJSON(json: any): LoginLog {
    return LoginLogToJSONTyped(json, false);
}

export function LoginLogToJSONTyped(value?: LoginLog | null, ignoreDiscriminator: boolean = false): any {
    if (value == null) {
        return value;
    }

    return {
        
        'id': value['id'],
        'time': ((value['time']).toISOString()),
        'userId': value['userId'],
    };
}

