import { Gender, Level, Role } from "./api";

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