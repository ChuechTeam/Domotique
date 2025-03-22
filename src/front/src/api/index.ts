import {Configuration, ResponseError, UsersApi} from "./gen";
import type {ErrorResponse} from "./gen";

// A classic fetch Response, with a bonus attribute: "errData", containing the error JSON if any.
export type JSErrResponse = Response & { errData: (ErrorResponse & { [k: string]: any }) | null };

// Our customized ResponseError; cast to it to get bonus attributes!
export type DomoResponseError = ResponseError & { response: JSErrResponse };

const config = new Configuration({
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
                const resp = ctx.response as JSErrResponse;
                try {
                    resp.errData = await ctx.response.json();
                } catch (e) {
                    console.error("Failed to read JSON error response into errData", e);
                    resp.errData = null;
                }

                return resp;
            }
        }
    }]
});

export default {
    /**
     * The users API (/api/users)
     */
    users: new UsersApi(config)
};

export * from "./gen";