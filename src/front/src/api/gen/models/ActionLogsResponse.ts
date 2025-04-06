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
import type { CompleteActionLog } from './CompleteActionLog';
import {
    CompleteActionLogFromJSON,
    CompleteActionLogFromJSONTyped,
    CompleteActionLogToJSON,
    CompleteActionLogToJSONTyped,
} from './CompleteActionLog';

/**
 * 
 * @export
 * @interface ActionLogsResponse
 */
export interface ActionLogsResponse {
    /**
     * 
     * @type {Array<CompleteActionLog>}
     * @memberof ActionLogsResponse
     */
    logs: Array<CompleteActionLog>;
}

/**
 * Check if a given object implements the ActionLogsResponse interface.
 */
export function instanceOfActionLogsResponse(value: object): value is ActionLogsResponse {
    if (!('logs' in value) || value['logs'] === undefined) return false;
    return true;
}

export function ActionLogsResponseFromJSON(json: any): ActionLogsResponse {
    return ActionLogsResponseFromJSONTyped(json, false);
}

export function ActionLogsResponseFromJSONTyped(json: any, ignoreDiscriminator: boolean): ActionLogsResponse {
    if (json == null) {
        return json;
    }
    return {
        
        'logs': ((json['logs'] as Array<any>).map(CompleteActionLogFromJSON)),
    };
}

export function ActionLogsResponseToJSON(json: any): ActionLogsResponse {
    return ActionLogsResponseToJSONTyped(json, false);
}

export function ActionLogsResponseToJSONTyped(value?: ActionLogsResponse | null, ignoreDiscriminator: boolean = false): any {
    if (value == null) {
        return value;
    }

    return {
        
        'logs': ((value['logs'] as Array<any>).map(CompleteActionLogToJSON)),
    };
}

