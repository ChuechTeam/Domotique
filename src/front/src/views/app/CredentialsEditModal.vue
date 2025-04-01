<script setup lang="ts">
import api, { ErrorResponse, isOk, ValidationErrorResponse } from '@/api';
import ValidationErrList from '@/components/ValidationErrList.vue';
import { useUserModule } from '@/modules/user';
import { useAuthStore } from '@/stores/auth';
import { useToast } from 'primevue';
import { defineProps, ref, computed } from 'vue';
import { useRouter } from 'vue-router';

// Set by vue router.
const props = defineProps({
    userId: {
        type: String,
        required: true
    }
});
const userId = parseInt(props.userId);

// Various dependencies
const router = useRouter(); // To go back
const auth = useAuthStore(); // To see our rights
const userModule = useUserModule(); // To update the user credentials
const toast = useToast(); // To show toast messages

// True when the modal dialog is visible. Set to false to hide it.
const visible = ref(true);
// Validation error from the API. Each field has a list of errors.
const err = ref<ValidationErrorResponse<any> | null>(null);
// The promise returned by the save function. Used to disable the button while saving.
const savePromise = ref<Promise<any>>(null);

// Email display only
const email = ref("");

// Password form fields
const oldPassword = ref("");
const newPassword = ref("");

// Check if current user is the same as the user being edited
const isCurrentUser = computed(() => userId === auth.userId);

// True when the save promise is running. 
const submitting = computed(() => savePromise.value != null);

// Called when the dialog closing animation finished.
function dialogClosed() {
    router.back();
}

async function save() {
    // Reset any previous errors
    err.value = null;

    // Basic client-side validation for new password
    if (!newPassword.value) {
        err.value = {
            message: "Veuillez entrer un nouveau mot de passe.",
            code: "VALIDATION_ERROR",
            data: { newPassword: ["Le mot de passe est requis."] }
        };
        return;
    }

    // For current user, old password is required
    if (isCurrentUser.value && !oldPassword.value) {
        err.value = {
            message: "Veuillez entrer votre mot de passe actuel.",
            code: "VALIDATION_ERROR",
            data: { oldPassword: ["Le mot de passe actuel est requis."] }
        };
        return;
    }

    try {
        savePromise.value = isCurrentUser.value
            ? userModule.updatePassword(userId, oldPassword.value, newPassword.value)
            : userModule.updatePassword(userId, undefined, newPassword.value);

        const result = await savePromise.value;

        if (isOk(result)) {
            // Password updated successfully
            toast.add({
                severity: 'success',
                summary: 'Mot de passe changé',
                detail: 'Le mot de passe a été mis à jour avec succès.',
                life: 5000
            })
            visible.value = false;
        } else {
            // Error during update
            err.value = result;
        }
    } catch (e) {
        // Unexpected error
        console.error("Error during save", e);
        err.value = { message: "Une erreur surprenante est survenue lors de la sauvegarde du mot de passe.", code: "_", data: {} };
    } finally {
        savePromise.value = null;
    }
}

// ------ INIT: Load the user email ---------
try {
    if (userId == auth.userId) {
        // Use current user's email
        email.value = auth.user.secret.email;
    } else {
        // Fetch the user to get their email
        const user = await api.users.findFullUser({ userId });
        if (user) {
            email.value = user.secret.email || "Email indisponible";
        } else {
            router.replace({ name: "profile", params: { userId } });
        }
    }
} catch (e) {
    console.error("Failed to fetch user email", e);
    err.value = { message: "Une erreur est survenue lors de la récupération des informations utilisateur.", code: "_", data: {} };
}
</script>
<template>
    <Dialog class="credentials-modal" v-model:visible="visible" modal @after-hide="dialogClosed"
        header="Modifier les identifiants">
        <template #default>
            <div class="-content">
                <div v-if="err?.message" :class="{ 'opacity-25': submitting, 'alert': true, 'alert-danger': true }">
                    {{ err.message }}
                </div>

                <div class="-email-section">
                    <FloatLabel variant="in">
                        <InputText v-model="email" disabled />
                        <label>Email</label>
                    </FloatLabel>
                </div>

                <div class="-password-section">
                    <h3>Changer le mot de passe</h3>
                    <div class="-password-fields">
                        <FloatLabel variant="in" v-if="isCurrentUser">
                            <InputText v-model="oldPassword" type="password"
                                :invalid="err?.data?.oldPassword?.length > 0" />
                            <label>Mot de passe actuel</label>
                            <ValidationErrList :errors="err?.data?.oldPassword" />
                        </FloatLabel>

                        <FloatLabel variant="in">
                            <InputText v-model="newPassword" type="password"
                                :invalid="err?.data?.newPassword?.length > 0" />
                            <label>Nouveau mot de passe</label>
                            <ValidationErrList :errors="err?.data?.newPassword" />
                        </FloatLabel>
                    </div>
                </div>
            </div>
        </template>
        <template #footer>
            <Button label="Sauvegarder" :disabled="submitting || !newPassword" @click="save" />
        </template>
    </Dialog>
</template>
<style>
.credentials-modal {
    min-width: min(100vw, 450px);

    & .-content {
        display: flex;
        flex-direction: column;
        gap: 16px;
    }

    & .-email-section,
    .-password-section {
        width: 100%;
    }

    & .-password-fields {
        display: flex;
        flex-direction: column;
        gap: 16px;
        margin-top: 8px;
    }

    & h3 {
        font-size: 1.2rem;
        margin: 8px 0;
    }

    & input {
        width: 100%;
    }
}
</style>