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
 * A response given by the API when something goes wrong.
 * @export
 * @interface ErrorResponse
 */
export interface ErrorResponse {
    /**
     * The error code. Can be used to identify the error in the client.
     * @type {string}
     * @memberof ErrorResponse
     */
    code: string;
    /**
     * Additional details about the error. Can be really useful for validation errors most of the time.
     * @type {object}
     * @memberof ErrorResponse
     */
    data: object;
    /**
     * The error message. Is localized (in French) and can be shown to the client for all errors except 400.
     * @type {string}
     * @memberof ErrorResponse
     */
    message: string;
}

/**
 * Check if a given object implements the ErrorResponse interface.
 */
export function instanceOfErrorResponse(value: object): value is ErrorResponse {
    if (!('code' in value) || value['code'] === undefined) return false;
    if (!('data' in value) || value['data'] === undefined) return false;
    if (!('message' in value) || value['message'] === undefined) return false;
    return true;
}

export function ErrorResponseFromJSON(json: any): ErrorResponse {
    return ErrorResponseFromJSONTyped(json, false);
}

export function ErrorResponseFromJSONTyped(json: any, ignoreDiscriminator: boolean): ErrorResponse {
    if (json == null) {
        return json;
    }
    return {
        
        'code': json['code'],
        'data': json['data'],
        'message': json['message'],
    };
}

export function ErrorResponseToJSON(json: any): ErrorResponse {
    return ErrorResponseToJSONTyped(json, false);
}

export function ErrorResponseToJSONTyped(value?: ErrorResponse | null, ignoreDiscriminator: boolean = false): any {
    if (value == null) {
        return value;
    }

    return {
        
        'code': value['code'],
        'data': value['data'],
        'message': value['message'],
    };
}

