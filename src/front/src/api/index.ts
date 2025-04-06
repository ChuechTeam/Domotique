import {Configuration, ResponseError, RoomsApi, UsersApi, LoginLogsApi, UserEventsApi, DevicesApi, DeviceTypesApi, ActionLogsApi, HealthApi, EnergyApi, InviteCodesApi} from "./gen";
import type {ErrorResponse} from "./gen";

// A classic fetch Response, with a bonus attribute: "errData", containing the error JSON if any.
export type JSErrResponse = Response & { errData: ErrorResponse };

// Our customized ResponseError; cast to it to get bonus attributes!
export type DomoResponseError = ResponseError & { response: JSErrResponse };

// Data contained for a validation error within the "data" attribute.
export type ValidationData<T> = {
    [k in keyof T]?: k extends (infer U)[] ? ValidationData<U>[] // array -> array of validation data
                     : (k extends Record<any, any> ? ValidationData<k> : string[]) // // object -> object of validation data; else string array
} 

// Like ErroResponse, but with validation data
export type ValidationErrorResponse<T> = ErrorResponse & {
    data: ValidationData<T>;
};

// You can add middlewares through this "config" variable: config.middleware.push(...)
export const config = new Configuration({
    // Make sure the base path is just the origin (i.e. http://domain.name:port part)
    basePath: location.origin,
    middleware: [{
        // Middleware that reads error JSON to put it into the response
        //
        // This way, when handling a RequestError, you can do
        // e.response.errData
        // to access the error data in json form.
        async post(ctx) {
            if (ctx.response.status >= 400 && ctx.response.status < 500) {
                const responseClone = ctx.response.clone();

                const resp = ctx.response as JSErrResponse;
                try {
                    resp.errData = (await responseClone.json()) ?? defaultErrorResponse;
                } catch (e) {
                    console.error("Failed to read JSON error response into errData. Using default error data", e);
                    resp.errData = defaultErrorResponse;
                }

                const cloneFunc = resp.clone;
                resp.clone = () => {
                    const cloned = cloneFunc.call(resp) as JSErrResponse;
                    cloned.errData = resp.errData;
                    return cloned;
                }

                return resp;
            }
        }
    }]
});

export const defaultErrorResponse: ErrorResponse = {
    code: "UNKNOWN_ERROR",
    message: "Une erreur est survenue.",
    data: null
}

/**
 * Finds API error data in the given JavaScript error.
 *
 * If there's no API error data in this error, returns `null`.
 *
 * @param error The error to look into.
 */
export function findErrData(error: Error): ErrorResponse | null {
    return findResponse(error)?.errData;
}

/**
 * Finds API error data in the given JavaScript error.
 *
 * Throws when there's no API error data in this error.
 *
 * @param error The error to look into.
 */
export function findErrDataOrThrow(error: Error): ErrorResponse {
    return findResponseOrThrow(error).errData;
}

/**
 * Finds response information in the given error, if any.
 *
 * Does not include 5xx errors (Internal Server Error) by default, unless you turn on `includeInternalErrors`.
 *
 * If there's no response information in this error, returns `null`.
 *
 * @param error the error which may be a ResponseError
 * @param includeInternalErrors whether to include 5xx errors
 */
export function findResponse(error: Error, includeInternalErrors = false): JSErrResponse | null {
    if (error instanceof ResponseError
        && (includeInternalErrors || error.response.status >= 400 || error.response.status < 500)) {
        return (error as DomoResponseError).response;
    }
    return null;
}

/**
 * Finds response information in the given error.
 *
 * Throws when it encounters 5xx errors (Internal Server Error) by default, unless you turn on `includeInternalErrors`.
 *
 * @param error the error which may be a ResponseError
 * @param includeInternalErrors whether to include 5xx errors
 */
export function findResponseOrThrow(error: Error, includeInternalErrors = false): JSErrResponse {
    if (error instanceof ResponseError
        && (includeInternalErrors || error.response.status >= 400 || error.response.status < 500)) {
        return (error as DomoResponseError).response;
    } else {
        throw error;
    }
}

export function isErr(x: any): x is ErrorResponse {
    return x && "code" in x && "message" in x && typeof x.message === "string";
}
export function isOk<T>(x: T | ErrorResponse): x is T {
    return !isErr(x);
}

// All available API modules are there.
export default {
    /**
     * The users API (/api/users)
     */
    users: new UsersApi(config),
    /**
     * The rooms API (/api/rooms)
     */
    rooms: new RoomsApi(config),
    devices: new DevicesApi(config),
    deviceTypes: new DeviceTypesApi(config),
    loginLogs: new LoginLogsApi(config),
    userEvents: new UserEventsApi(config),
    actionLogs: new ActionLogsApi(config),
    health: new HealthApi(config),
    energy: new EnergyApi(config),
    inviteCodes: new InviteCodesApi(config)
};

// Export everything from the generated API (types, mainly)
export * from "./gen";