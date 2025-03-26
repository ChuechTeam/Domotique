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
  CompleteRoom,
  ErrorResponse,
  RoomInput,
  RoomsResponse,
} from '../models/index';
import {
    CompleteRoomFromJSON,
    CompleteRoomToJSON,
    ErrorResponseFromJSON,
    ErrorResponseToJSON,
    RoomInputFromJSON,
    RoomInputToJSON,
    RoomsResponseFromJSON,
    RoomsResponseToJSON,
} from '../models/index';

export interface CreateRoomRequest {
    roomInput: RoomInput;
}

export interface DeleteRoomRequest {
    roomId: number;
}

export interface GetRoomByIdRequest {
    roomId: number;
}

export interface UpdateRoomRequest {
    roomId: number;
    roomInput: RoomInput;
}

/**
 * 
 */
export class RoomsApi extends runtime.BaseAPI {

    /**
     * Creates a new room.
     * Create room
     */
    async createRoomRaw(requestParameters: CreateRoomRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<CompleteRoom>> {
        if (requestParameters['roomInput'] == null) {
            throw new runtime.RequiredError(
                'roomInput',
                'Required parameter "roomInput" was null or undefined when calling createRoom().'
            );
        }

        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        headerParameters['Content-Type'] = 'application/json';

        const response = await this.request({
            path: `/api/rooms/`,
            method: 'POST',
            headers: headerParameters,
            query: queryParameters,
            body: RoomInputToJSON(requestParameters['roomInput']),
        }, initOverrides);

        return new runtime.JSONApiResponse(response, (jsonValue) => CompleteRoomFromJSON(jsonValue));
    }

    /**
     * Creates a new room.
     * Create room
     */
    async createRoom(requestParameters: CreateRoomRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<CompleteRoom> {
        const response = await this.createRoomRaw(requestParameters, initOverrides);
        return await response.value();
    }

    /**
     * Deletes a room.
     * Delete room
     */
    async deleteRoomRaw(requestParameters: DeleteRoomRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<void>> {
        if (requestParameters['roomId'] == null) {
            throw new runtime.RequiredError(
                'roomId',
                'Required parameter "roomId" was null or undefined when calling deleteRoom().'
            );
        }

        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        const response = await this.request({
            path: `/api/rooms/{roomId}`.replace(`{${"roomId"}}`, encodeURIComponent(String(requestParameters['roomId']))),
            method: 'DELETE',
            headers: headerParameters,
            query: queryParameters,
        }, initOverrides);

        return new runtime.VoidApiResponse(response);
    }

    /**
     * Deletes a room.
     * Delete room
     */
    async deleteRoom(requestParameters: DeleteRoomRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<void> {
        await this.deleteRoomRaw(requestParameters, initOverrides);
    }

    /**
     * Gets a room by its ID.
     * Get room by ID
     */
    async getRoomByIdRaw(requestParameters: GetRoomByIdRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<CompleteRoom>> {
        if (requestParameters['roomId'] == null) {
            throw new runtime.RequiredError(
                'roomId',
                'Required parameter "roomId" was null or undefined when calling getRoomById().'
            );
        }

        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        const response = await this.request({
            path: `/api/rooms/{roomId}`.replace(`{${"roomId"}}`, encodeURIComponent(String(requestParameters['roomId']))),
            method: 'GET',
            headers: headerParameters,
            query: queryParameters,
        }, initOverrides);

        return new runtime.JSONApiResponse(response, (jsonValue) => CompleteRoomFromJSON(jsonValue));
    }

    /**
     * Gets a room by its ID.
     * Get room by ID
     */
    async getRoomById(requestParameters: GetRoomByIdRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<CompleteRoom | null | undefined > {
        const response = await this.getRoomByIdRaw(requestParameters, initOverrides);
        switch (response.raw.status) {
            case 200:
                return await response.value();
            case 204:
                return null;
            default:
                return await response.value();
        }
    }

    /**
     * Gets all rooms from the database.
     * Get rooms
     */
    async getRoomsRaw(initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<RoomsResponse>> {
        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        const response = await this.request({
            path: `/api/rooms/`,
            method: 'GET',
            headers: headerParameters,
            query: queryParameters,
        }, initOverrides);

        return new runtime.JSONApiResponse(response, (jsonValue) => RoomsResponseFromJSON(jsonValue));
    }

    /**
     * Gets all rooms from the database.
     * Get rooms
     */
    async getRooms(initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<RoomsResponse> {
        const response = await this.getRoomsRaw(initOverrides);
        return await response.value();
    }

    /**
     * Updates an existing room.
     * Update room
     */
    async updateRoomRaw(requestParameters: UpdateRoomRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<CompleteRoom>> {
        if (requestParameters['roomId'] == null) {
            throw new runtime.RequiredError(
                'roomId',
                'Required parameter "roomId" was null or undefined when calling updateRoom().'
            );
        }

        if (requestParameters['roomInput'] == null) {
            throw new runtime.RequiredError(
                'roomInput',
                'Required parameter "roomInput" was null or undefined when calling updateRoom().'
            );
        }

        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        headerParameters['Content-Type'] = 'application/json';

        const response = await this.request({
            path: `/api/rooms/{roomId}`.replace(`{${"roomId"}}`, encodeURIComponent(String(requestParameters['roomId']))),
            method: 'PUT',
            headers: headerParameters,
            query: queryParameters,
            body: RoomInputToJSON(requestParameters['roomInput']),
        }, initOverrides);

        return new runtime.JSONApiResponse(response, (jsonValue) => CompleteRoomFromJSON(jsonValue));
    }

    /**
     * Updates an existing room.
     * Update room
     */
    async updateRoom(requestParameters: UpdateRoomRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<CompleteRoom> {
        const response = await this.updateRoomRaw(requestParameters, initOverrides);
        return await response.value();
    }

}
