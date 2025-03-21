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
  ChangePasswordInput,
  CompleteUser,
  ErrorResponse,
  LoginInput,
  ProfileSearchOutput,
  RegisterInput,
  UpdateProfileInput,
  UserProfile,
} from '../models/index';
import {
    ChangePasswordInputFromJSON,
    ChangePasswordInputToJSON,
    CompleteUserFromJSON,
    CompleteUserToJSON,
    ErrorResponseFromJSON,
    ErrorResponseToJSON,
    LoginInputFromJSON,
    LoginInputToJSON,
    ProfileSearchOutputFromJSON,
    ProfileSearchOutputToJSON,
    RegisterInputFromJSON,
    RegisterInputToJSON,
    UpdateProfileInputFromJSON,
    UpdateProfileInputToJSON,
    UserProfileFromJSON,
    UserProfileToJSON,
} from '../models/index';

export interface ChangePasswordRequest {
    changePasswordInput: ChangePasswordInput;
}

export interface ConfirmEmailRequest {
    token: number;
    user: number;
}

export interface FindUserRequest {
    userId: number;
}

export interface LoginRequest {
    loginInput: LoginInput;
}

export interface RegisterRequest {
    registerInput: RegisterInput;
}

export interface SearchUsersRequest {
    fullName: string;
}

export interface UpdateProfileRequest {
    userId: string;
    updateProfileInput: UpdateProfileInput;
}

/**
 * 
 */
export class UsersApi extends runtime.BaseAPI {

    /**
     * Change the password of the currently authenticated user.
     * Change password
     */
    async changePasswordRaw(requestParameters: ChangePasswordRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<void>> {
        if (requestParameters['changePasswordInput'] == null) {
            throw new runtime.RequiredError(
                'changePasswordInput',
                'Required parameter "changePasswordInput" was null or undefined when calling changePassword().'
            );
        }

        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        headerParameters['Content-Type'] = 'application/json';

        const response = await this.request({
            path: `/api/users/me/password`,
            method: 'POST',
            headers: headerParameters,
            query: queryParameters,
            body: ChangePasswordInputToJSON(requestParameters['changePasswordInput']),
        }, initOverrides);

        return new runtime.VoidApiResponse(response);
    }

    /**
     * Change the password of the currently authenticated user.
     * Change password
     */
    async changePassword(requestParameters: ChangePasswordRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<void> {
        await this.changePasswordRaw(requestParameters, initOverrides);
    }

    /**
     * 
     * Confirm email
     */
    async confirmEmailRaw(requestParameters: ConfirmEmailRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<void>> {
        if (requestParameters['token'] == null) {
            throw new runtime.RequiredError(
                'token',
                'Required parameter "token" was null or undefined when calling confirmEmail().'
            );
        }

        if (requestParameters['user'] == null) {
            throw new runtime.RequiredError(
                'user',
                'Required parameter "user" was null or undefined when calling confirmEmail().'
            );
        }

        const queryParameters: any = {};

        if (requestParameters['token'] != null) {
            queryParameters['token'] = requestParameters['token'];
        }

        if (requestParameters['user'] != null) {
            queryParameters['user'] = requestParameters['user'];
        }

        const headerParameters: runtime.HTTPHeaders = {};

        const response = await this.request({
            path: `/confirmEmail`,
            method: 'GET',
            headers: headerParameters,
            query: queryParameters,
        }, initOverrides);

        return new runtime.VoidApiResponse(response);
    }

    /**
     * 
     * Confirm email
     */
    async confirmEmail(requestParameters: ConfirmEmailRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<void> {
        await this.confirmEmailRaw(requestParameters, initOverrides);
    }

    /**
     * Gets a user by their ID, and return their public data.
     * Get user profile by ID
     */
    async findUserRaw(requestParameters: FindUserRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<UserProfile>> {
        if (requestParameters['userId'] == null) {
            throw new runtime.RequiredError(
                'userId',
                'Required parameter "userId" was null or undefined when calling findUser().'
            );
        }

        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        const response = await this.request({
            path: `/api/users/{userId}`.replace(`{${"userId"}}`, encodeURIComponent(String(requestParameters['userId']))),
            method: 'GET',
            headers: headerParameters,
            query: queryParameters,
        }, initOverrides);

        return new runtime.JSONApiResponse(response, (jsonValue) => UserProfileFromJSON(jsonValue));
    }

    /**
     * Gets a user by their ID, and return their public data.
     * Get user profile by ID
     */
    async findUser(requestParameters: FindUserRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<UserProfile | null | undefined > {
        const response = await this.findUserRaw(requestParameters, initOverrides);
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
     * Log in a user with an email and a password.
     * Log in
     */
    async loginRaw(requestParameters: LoginRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<CompleteUser>> {
        if (requestParameters['loginInput'] == null) {
            throw new runtime.RequiredError(
                'loginInput',
                'Required parameter "loginInput" was null or undefined when calling login().'
            );
        }

        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        headerParameters['Content-Type'] = 'application/json';

        const response = await this.request({
            path: `/api/users/login`,
            method: 'POST',
            headers: headerParameters,
            query: queryParameters,
            body: LoginInputToJSON(requestParameters['loginInput']),
        }, initOverrides);

        return new runtime.JSONApiResponse(response, (jsonValue) => CompleteUserFromJSON(jsonValue));
    }

    /**
     * Log in a user with an email and a password.
     * Log in
     */
    async login(requestParameters: LoginRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<CompleteUser> {
        const response = await this.loginRaw(requestParameters, initOverrides);
        return await response.value();
    }

    /**
     * Log out the current user from the app.
     * Log out
     */
    async logoutRaw(initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<void>> {
        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        const response = await this.request({
            path: `/api/users/logout`,
            method: 'POST',
            headers: headerParameters,
            query: queryParameters,
        }, initOverrides);

        return new runtime.VoidApiResponse(response);
    }

    /**
     * Log out the current user from the app.
     * Log out
     */
    async logout(initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<void> {
        await this.logoutRaw(initOverrides);
    }

    /**
     * Gets the currently authenticated user\'s complete data.
     * Get my user profile
     */
    async meRaw(initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<CompleteUser>> {
        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        const response = await this.request({
            path: `/api/users/me`,
            method: 'GET',
            headers: headerParameters,
            query: queryParameters,
        }, initOverrides);

        return new runtime.JSONApiResponse(response, (jsonValue) => CompleteUserFromJSON(jsonValue));
    }

    /**
     * Gets the currently authenticated user\'s complete data.
     * Get my user profile
     */
    async me(initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<CompleteUser> {
        const response = await this.meRaw(initOverrides);
        return await response.value();
    }

    /**
     * Register a new user with an email and a password.
     * Register
     */
    async registerRaw(requestParameters: RegisterRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<CompleteUser>> {
        if (requestParameters['registerInput'] == null) {
            throw new runtime.RequiredError(
                'registerInput',
                'Required parameter "registerInput" was null or undefined when calling register().'
            );
        }

        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        headerParameters['Content-Type'] = 'application/json';

        const response = await this.request({
            path: `/api/users/`,
            method: 'POST',
            headers: headerParameters,
            query: queryParameters,
            body: RegisterInputToJSON(requestParameters['registerInput']),
        }, initOverrides);

        return new runtime.JSONApiResponse(response, (jsonValue) => CompleteUserFromJSON(jsonValue));
    }

    /**
     * Register a new user with an email and a password.
     * Register
     */
    async register(requestParameters: RegisterRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<CompleteUser> {
        const response = await this.registerRaw(requestParameters, initOverrides);
        return await response.value();
    }

    /**
     * Search for users by their first name, last name, or email.
     * Search users
     */
    async searchUsersRaw(requestParameters: SearchUsersRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<ProfileSearchOutput>> {
        if (requestParameters['fullName'] == null) {
            throw new runtime.RequiredError(
                'fullName',
                'Required parameter "fullName" was null or undefined when calling searchUsers().'
            );
        }

        const queryParameters: any = {};

        if (requestParameters['fullName'] != null) {
            queryParameters['fullName'] = requestParameters['fullName'];
        }

        const headerParameters: runtime.HTTPHeaders = {};

        const response = await this.request({
            path: `/api/users/`,
            method: 'GET',
            headers: headerParameters,
            query: queryParameters,
        }, initOverrides);

        return new runtime.JSONApiResponse(response, (jsonValue) => ProfileSearchOutputFromJSON(jsonValue));
    }

    /**
     * Search for users by their first name, last name, or email.
     * Search users
     */
    async searchUsers(requestParameters: SearchUsersRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<ProfileSearchOutput> {
        const response = await this.searchUsersRaw(requestParameters, initOverrides);
        return await response.value();
    }

    /**
     * Update the profile of the currently authenticated user.
     * Update profile
     */
    async updateProfileRaw(requestParameters: UpdateProfileRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<UserProfile>> {
        if (requestParameters['userId'] == null) {
            throw new runtime.RequiredError(
                'userId',
                'Required parameter "userId" was null or undefined when calling updateProfile().'
            );
        }

        if (requestParameters['updateProfileInput'] == null) {
            throw new runtime.RequiredError(
                'updateProfileInput',
                'Required parameter "updateProfileInput" was null or undefined when calling updateProfile().'
            );
        }

        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        headerParameters['Content-Type'] = 'application/json';

        const response = await this.request({
            path: `/api/users/me/profile`.replace(`{${"userId"}}`, encodeURIComponent(String(requestParameters['userId']))),
            method: 'POST',
            headers: headerParameters,
            query: queryParameters,
            body: UpdateProfileInputToJSON(requestParameters['updateProfileInput']),
        }, initOverrides);

        return new runtime.JSONApiResponse(response, (jsonValue) => UserProfileFromJSON(jsonValue));
    }

    /**
     * Update the profile of the currently authenticated user.
     * Update profile
     */
    async updateProfile(requestParameters: UpdateProfileRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<UserProfile> {
        const response = await this.updateProfileRaw(requestParameters, initOverrides);
        return await response.value();
    }

}
