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
  ErrorResponse,
  InviteCode,
  InviteCodeInput,
  InviteCodesResponse,
} from '../models/index';
import {
    ErrorResponseFromJSON,
    ErrorResponseToJSON,
    InviteCodeFromJSON,
    InviteCodeToJSON,
    InviteCodeInputFromJSON,
    InviteCodeInputToJSON,
    InviteCodesResponseFromJSON,
    InviteCodesResponseToJSON,
} from '../models/index';

export interface CreateInviteCodeRequest {
    inviteCodeInput: InviteCodeInput;
}

export interface DeleteInviteCodeRequest {
    id: string;
}

/**
 * 
 */
export class InviteCodesApi extends runtime.BaseAPI {

    /**
     * Creates a new invite code with a randomly generated ID.
     * Create invite code
     */
    async createInviteCodeRaw(requestParameters: CreateInviteCodeRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<InviteCode>> {
        if (requestParameters['inviteCodeInput'] == null) {
            throw new runtime.RequiredError(
                'inviteCodeInput',
                'Required parameter "inviteCodeInput" was null or undefined when calling createInviteCode().'
            );
        }

        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        headerParameters['Content-Type'] = 'application/json';

        const response = await this.request({
            path: `/api/invite-codes/`,
            method: 'POST',
            headers: headerParameters,
            query: queryParameters,
            body: InviteCodeInputToJSON(requestParameters['inviteCodeInput']),
        }, initOverrides);

        return new runtime.JSONApiResponse(response, (jsonValue) => InviteCodeFromJSON(jsonValue));
    }

    /**
     * Creates a new invite code with a randomly generated ID.
     * Create invite code
     */
    async createInviteCode(requestParameters: CreateInviteCodeRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<InviteCode> {
        const response = await this.createInviteCodeRaw(requestParameters, initOverrides);
        return await response.value();
    }

    /**
     * Deletes an invite code.
     * Delete invite code
     */
    async deleteInviteCodeRaw(requestParameters: DeleteInviteCodeRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<void>> {
        if (requestParameters['id'] == null) {
            throw new runtime.RequiredError(
                'id',
                'Required parameter "id" was null or undefined when calling deleteInviteCode().'
            );
        }

        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        const response = await this.request({
            path: `/api/invite-codes/{id}`.replace(`{${"id"}}`, encodeURIComponent(String(requestParameters['id']))),
            method: 'DELETE',
            headers: headerParameters,
            query: queryParameters,
        }, initOverrides);

        return new runtime.VoidApiResponse(response);
    }

    /**
     * Deletes an invite code.
     * Delete invite code
     */
    async deleteInviteCode(requestParameters: DeleteInviteCodeRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<void> {
        await this.deleteInviteCodeRaw(requestParameters, initOverrides);
    }

    /**
     * Gets all invite codes from the database.
     * Get invite codes
     */
    async getInviteCodesRaw(initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<InviteCodesResponse>> {
        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        const response = await this.request({
            path: `/api/invite-codes/`,
            method: 'GET',
            headers: headerParameters,
            query: queryParameters,
        }, initOverrides);

        return new runtime.JSONApiResponse(response, (jsonValue) => InviteCodesResponseFromJSON(jsonValue));
    }

    /**
     * Gets all invite codes from the database.
     * Get invite codes
     */
    async getInviteCodes(initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<InviteCodesResponse> {
        const response = await this.getInviteCodesRaw(initOverrides);
        return await response.value();
    }

}
