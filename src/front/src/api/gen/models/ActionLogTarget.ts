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


/**
 * 
 * @export
 */
export const ActionLogTarget = {
    Device: 'DEVICE',
    User: 'USER',
    Room: 'ROOM',
    DeviceType: 'DEVICE_TYPE'
} as const;
export type ActionLogTarget = typeof ActionLogTarget[keyof typeof ActionLogTarget];


export function instanceOfActionLogTarget(value: any): boolean {
    for (const key in ActionLogTarget) {
        if (Object.prototype.hasOwnProperty.call(ActionLogTarget, key)) {
            if (ActionLogTarget[key as keyof typeof ActionLogTarget] === value) {
                return true;
            }
        }
    }
    return false;
}

export function ActionLogTargetFromJSON(json: any): ActionLogTarget {
    return ActionLogTargetFromJSONTyped(json, false);
}

export function ActionLogTargetFromJSONTyped(json: any, ignoreDiscriminator: boolean): ActionLogTarget {
    return json as ActionLogTarget;
}

export function ActionLogTargetToJSON(value?: ActionLogTarget | null): any {
    return value as any;
}

export function ActionLogTargetToJSONTyped(value: any, ignoreDiscriminator: boolean): ActionLogTarget {
    return value as ActionLogTarget;
}

