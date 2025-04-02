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
    "TEMPERATURE": "Température",
    "HEART_RATE": "Fréquence cardiaque",
    "BLOOD_PRESSURE": "Tension artérielle",
    "BLOOD_OXYGEN": "Taux d'oxygène dans le sang",
    "BLOOD_GLUCOSE": "Taux de sucre dans le sang",
    "FAT_PERCENTAGE": "Taux de graisse corporelle",
    "STEPS": "Nombre de pas",
    "LAST_SLEEP_DURATION": "Durée du dernier sommeil",
    "MAX_VO2": "VO2 max",
    "RESPIRATORY_RATE": "Fréquence respiratoire",
    "BODY_TEMPERATURE": "Température corporelle",
    "BODY_WEIGHT": "Poids"
}

// All attribute type enum values
export const attributeTypes = Object.values(AttributeType)

// Links each attribute with the type of the value it describes
export const attributeTypeContents: Record<AttributeType, NumberConstructor | StringConstructor | BooleanConstructor> = {
    "ACTIVITY_DURATION": Number,
    "CALORIES_BURNED": Number,
    "HUMIDITY": Number,
    "TEMPERATURE": Number,
    "HEART_RATE": Number,
    "BLOOD_PRESSURE": Number,
    "BLOOD_OXYGEN": Number,
    "BLOOD_GLUCOSE": Number,
    "FAT_PERCENTAGE": Number,
    "STEPS": Number,
    "LAST_SLEEP_DURATION": Number,
    "MAX_VO2": Number,
    "RESPIRATORY_RATE": Number,
    "BODY_TEMPERATURE": Number,
    "BODY_WEIGHT": Number
} as const;

// https://primevue.org/inputnumber/#prefixsuffix
export const attributeTypeFormats = {
    "ACTIVITY_DURATION": {suffix: " min"},
    "HUMIDITY": {suffix: " %"},
    "TEMPERATURE": {suffix: "°C"},
    "CALORIES_BURNED": {suffix: " kcal"},
    "HEART_RATE": {suffix: " bpm"},
    "BLOOD_PRESSURE": {suffix: " mmHg"},
    "BLOOD_OXYGEN": {suffix: " %"},
    "BLOOD_GLUCOSE": {suffix: " mg/dL"},
    "FAT_PERCENTAGE": {suffix: " %"},
    "STEPS": {suffix: ""},
    "LAST_SLEEP_DURATION": {suffix: " min"},
    "MAX_VO2": {suffix: " mL/kg/min"},
    "RESPIRATORY_RATE": {suffix: "/min"},
    "BODY_TEMPERATURE": {suffix: "°C"},
    "BODY_WEIGHT": {suffix: " kg"}
}

export function formatAttribute(type: AttributeType, val: any) {
    if (val === null) {
        return "Privé";
    }

    switch (type) {
        case "ACTIVITY_DURATION":
            return `${val} min`
        case "CALORIES_BURNED":
            return `${val} kcal`
        case "HUMIDITY":
            return `${val} %`
        case "TEMPERATURE":
            return `${val}°C`
        case "HEART_RATE":
            return `${val} bpm`
        case "BLOOD_PRESSURE":
            return `${val} mmHg`
        case "BLOOD_OXYGEN":
            return `${val} %`
        case "BLOOD_GLUCOSE":
            return `${val} mg/dL`
        case "FAT_PERCENTAGE":
            return `${val} %`
        case "STEPS":
            return `${val}`
        case "LAST_SLEEP_DURATION":
            return `${val} min`
        case "MAX_VO2":
            return `${val} mL/kg/min`
        case "RESPIRATORY_RATE":
            return `${val}/min`
        case "BODY_TEMPERATURE":
            return `${val}°C`
        case "BODY_WEIGHT":
            return `${val} kg`
    }
}