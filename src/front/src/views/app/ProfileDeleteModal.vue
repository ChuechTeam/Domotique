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

// Password form fields
const password = ref("");

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

    // For current user, old password is required
    if (isCurrentUser.value && !password.value) {
        err.value = {
            message: "Veuillez entrer votre mot de passe actuel.",
            code: "VALIDATION_ERROR",
            data: { oldPassword: ["Le mot de passe actuel est requis."] }
        };
        return;
    }

    try {
        savePromise.value = userModule.deleteAccount(userId)

        const result = await savePromise.value;

        if (isOk(result)) {
            // Password updated successfully
            toast.add({
                severity: 'success',
                summary: 'Compte supprimé',
                detail: 'Le compte utilisateur a bien été supprimé.',
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
        err.value = { message: "Une erreur surprenante est survenue !", code: "_", data: {} };
    } finally {
        savePromise.value = null;
    }
}
</script>
<template>
    <Dialog class="credentials-modal" v-model:visible="visible" modal @after-hide="dialogClosed"
        header="Supprimer le compte">
        <template #default>
            <div class="-content">
                <div v-if="err?.message" :class="{ 'opacity-25': submitting, 'alert': true, 'alert-danger': true }">
                    {{ err.message }}
                </div>

                <p>Attention, vous allez supprimer ce compte !</p>

                <p>Tous les appareils qui sont associés à ce compte seront toujours présents.</p>

                <div class="-password-section" v-if="isCurrentUser">
                    <h3>Inscrire le mot de passe</h3>
                    <div class="-password-fields">
                        <FloatLabel variant="in">
                            <InputText v-model="password" type="password"
                                :invalid="err?.data?.newPassword?.length > 0" />
                            <label>Confirmation du mot de passe</label>
                            <ValidationErrList :errors="err?.data?.newPassword" />
                        </FloatLabel>
                    </div>
                </div>
            </div>
        </template>
        <template #footer>
            <Button label="Supprimer" severity="danger" :disabled="submitting" @click="save" />
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