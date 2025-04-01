import { AttributeType, DeviceCategory, Gender, Level, Role } from "./api";

// Gender enum -> Display name
export const genderLabels: Record<Gender, string> = {
    "FEMALE": "Femme",
    "MALE": "Homme",
    "UNDISCLOSED": "Autre",
}

// All gender enum values
export const genders = Object.values(Gender)

// Role enum -> Display name
export const roleLabels: Record<Role, string> = {
    "RESIDENT": "Habitant",
    "CAREGIVER": "Soignant",
    "ADMIN": "Administrateur",
}

// All role enum values
export const roles = Object.values(Role)

// Level enum -> Display name
export const levelLabels: Record<Level, string> = {
    "BEGINNER": "Débutant",
    "INTERMEDIATE": "Intermédiaire",
    "ADVANCED": "Avancé",
    "EXPERT": "Expert"
};

// All level enum values
export const levels = Object.values(Level)

// DeviceCategory enum -> Display name
export const deviceCategoryLabels: Record<DeviceCategory, string> = {
    "GARDENING": "Potager",
    "HEALTH": "Santé",
    "KITCHEN": "Cuisine",
    "LIGHTING": "Lumière",
    "SECURITY": "Sécurité",
    "TEMPERATURE_REGULATION": "Chauffage & Climatisation",
    "SPORT": "Sport",
    "OTHER": "Autre",
}

// All device category enum values
export const deviceCategories = Object.values(DeviceCategory)

// AttributeType enum -> Display name
export const attributeTypeLabels: Record<AttributeType, string> = {
    "ACTIVITY_DURATION": "Durée d'activité",
    "CALORIES_BURNED": "Calories brûlées",
    "HUMIDITY": "Humidité",
    "TEMPERATURE": "Température"
}

// All attribute type enum values
export const attributeTypes = Object.values(AttributeType)

// Links each attribute with the type of the value it describes
export const attributeTypeContents: Record<AttributeType, NumberConstructor | StringConstructor | BooleanConstructor> = {
    "ACTIVITY_DURATION": Number,
    "CALORIES_BURNED": Number,
    "HUMIDITY": Number,
    "TEMPERATURE": Number
} as const;

// https://primevue.org/inputnumber/#prefixsuffix
export const attributeTypeFormats = {
    "ACTIVITY_DURATION": {suffix: " min"},
    "HUMIDITY": {suffix: " %"},
    "TEMPERATURE": {suffix: " °C"},
    "CALORIES_BURNED": {suffix: " kcal"},
}

export function formatAttribute(type: AttributeType, val: any) {
    switch (type) {
        case "ACTIVITY_DURATION":
            return `${val} min`
        case "CALORIES_BURNED":
            return `${val} kcal`
        case "HUMIDITY":
            return `${val} %`
        case "TEMPERATURE":
            return `${val} °C`
    }
}