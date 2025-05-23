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


import * as runtime from '../runtime';
import type {
  ActionLogOperation,
  ActionLogTarget,
  ActionLogsResponse,
  ErrorResponse,
} from '../models/index';
import {
    ActionLogOperationFromJSON,
    ActionLogOperationToJSON,
    ActionLogTargetFromJSON,
    ActionLogTargetToJSON,
    ActionLogsResponseFromJSON,
    ActionLogsResponseToJSON,
    ErrorResponseFromJSON,
    ErrorResponseToJSON,
} from '../models/index';

export interface GetActionLogsRequest {
    userId?: number;
    desc?: boolean;
    operation?: ActionLogOperation;
    target?: ActionLogTarget;
}

/**
 * 
 */
export class ActionLogsApi extends runtime.BaseAPI {

    /**
     * Returns a list of action logs filtered by the provided parameters
     * Get filtered action logs
     */
    async getActionLogsRaw(requestParameters: GetActionLogsRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<ActionLogsResponse>> {
        const queryParameters: any = {};

        if (requestParameters['userId'] != null) {
            queryParameters['userId'] = requestParameters['userId'];
        }

        if (requestParameters['desc'] != null) {
            queryParameters['desc'] = requestParameters['desc'];
        }

        if (requestParameters['operation'] != null) {
            queryParameters['operation'] = requestParameters['operation'];
        }

        if (requestParameters['target'] != null) {
            queryParameters['target'] = requestParameters['target'];
        }

        const headerParameters: runtime.HTTPHeaders = {};

        const response = await this.request({
            path: `/api/action-logs/`,
            method: 'GET',
            headers: headerParameters,
            query: queryParameters,
        }, initOverrides);

        return new runtime.JSONApiResponse(response, (jsonValue) => ActionLogsResponseFromJSON(jsonValue));
    }

    /**
     * Returns a list of action logs filtered by the provided parameters
     * Get filtered action logs
     */
    async getActionLogs(requestParameters: GetActionLogsRequest = {}, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<ActionLogsResponse> {
        const response = await this.getActionLogsRaw(requestParameters, initOverrides);
        return await response.value();
    }

}
