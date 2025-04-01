<script setup lang="ts">
import api, { CompleteUser, ErrorResponse, isOk, PatchProfileInput, UserProfile, ValidationErrorResponse } from '@/api';
import ValidationErrList from '@/components/ValidationErrList.vue';
import { genderLabels, genders, levelLabels, levels, roleLabels, roles } from '@/labels';
import { useUserModule } from '@/modules/user';
import { useAuthStore } from '@/stores/auth';
import { useToast } from 'primevue';
import { defineProps, ref, computed, watch, toRaw } from 'vue';
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
const userModule = useUserModule(); // To update the user profile
const toast = useToast(); // To show toast messages

// True when user == null AND we got an error while fetching the user.
const fetchFailed = ref(false);
// The user data is loaded here.
const profile = ref<UserProfile>(null);
// True when the modal dialog is visible. Set to false to hide it.
const visible = ref(true);
// Validation error from the API. Each field has a list of errors.
const err = ref<ValidationErrorResponse<PatchProfileInput> | null>(null);
// The promise returned by the save function. Used to disable the button while saving.
const savePromise = ref<Promise<UserProfile | ErrorResponse>>(null);

// An event emitted when the profile has been saved successfully
const emit = defineEmits(["profile-update"]);

// True when the save promise is running. 
const submitting = computed(() => savePromise.value != null);

// Called when the dialog closing animation finished.
function dialogClosed() {
    router.back();  // todo: push if profile wasn't prev route
}

async function save() {
    const p = profile.value;

    const commonValues = {
        firstName: p.firstName,
        lastName: p.lastName,
        gender: p.gender
    };
    const adminValues = auth.canAdminister ? {
        role: p.role,
        level: p.level,
        points: p.points
    } : {};

    savePromise.value = userModule.updateProfile(p.id, {
        ...commonValues,
        ...adminValues
    })

    try {
        const result = await savePromise.value;

        if (isOk(result)) {
            // User updated successfully
            toast.add({
                severity: 'success',
                summary: 'Profil modifié',
                detail: auth.userId === userId ? 'Votre profil a bien été sauvegardé !' : `Le profil de ${p.firstName} a bien été modifié !`,
                life: 5000
            })
            visible.value = false;
            emit("profile-update", result);
        } else {
            // Error during update
            err.value = result;
        }
    } catch (e) {
        // Unexpected error
        console.error("Error during save", e);
        err.value = { message: "Une erreur surprenante est survenue lors de la sauvegarde du profil.", code: "_", data: {} };
    } finally {
        savePromise.value = null;
    }
}

// ------ INIT: Load the full user data ---------
try {
    if (userId == auth.userId) {
        // Clone our current profile so we can edit it without changing the current user.
        profile.value = structuredClone(toRaw(auth.user.profile))
    } else {
        profile.value = await api.users.findUser({ userId });
    }

    if (profile.value == null) {
        // User not found. Replace the current route with the profile route.
        router.replace({ name: "profile", params: { userId } });
    }
} catch (e) {
    console.error("failed to fetch user", e);
    fetchFailed.value = true;
}

// When the admin sets the role to "ADMIN", make sure the user is forced to be an expert.
watch(() => profile?.value?.role, (val, _) => {
    if (val && val === "ADMIN") {
        profile.value.level = "EXPERT"
    }
})
</script>
<template>
    <Dialog class="profile-edit-modal" v-model:visible="visible" modal @after-hide="dialogClosed"
        header="Modifier le profil">
        <template #default>
            <div class="-content" v-if="profile">
                <div v-if="err?.message" :class="{ 'opacity-25': submitting, 'alert': true, 'alert-danger': true }">
                    {{ err.message }}
                </div>

                <div class="-names">
                    <FloatLabel variant="in">
                        <InputText v-model="profile.firstName" :invalid="err?.data?.firstName?.length > 0" />
                        <label>Prénom</label>
                        <ValidationErrList :errors="err?.data?.firstName" />
                    </FloatLabel>

                    <FloatLabel variant="in">
                        <InputText v-model="profile.lastName" :invalid="err?.data?.lastName?.length > 0" />
                        <label>Nom</label>
                        <ValidationErrList :errors="err?.data?.lastName" />
                    </FloatLabel>
                </div>

                <FloatLabel variant="in" class="-gender">
                    <Select :options="genders" :option-label="x => genderLabels[x]" v-model="profile.gender"
                        class="w-100" :invalid="err?.data?.gender?.length > 0" />
                    <label>Genre</label>
                    <ValidationErrList :errors="err?.data?.gender" />
                </FloatLabel>

                <div class="-admin" v-if="auth.canAdminister">
                    <h3>Contrôles administrateur</h3>
                    <div class="-admin-fields -through">
                        <FloatLabel variant="in">
                            <Select v-model="profile.role" :options="roles" :option-label="x => roleLabels[x]"
                                class="w-100" :invalid="err?.data?.role?.length > 0" />
                            <label>Rôle</label>
                            <ValidationErrList :errors="err?.data?.role" />
                        </FloatLabel>

                        <FloatLabel variant="in">
                            <Select v-model="profile.level" :options="levels" :option-label="x => levelLabels[x]"
                                class="w-100" :invalid="err?.data?.level?.length > 0"
                                :disabled="profile.role === 'ADMIN'" />
                            <label>Niveau</label>
                            <ValidationErrList :errors="err?.data?.level" />
                        </FloatLabel>

                        <FloatLabel variant="in">
                            <InputNumber type="number" v-model="profile.points" show-buttons
                                :invalid="err?.data?.points?.length > 0" class="w-100" />
                            <label>Points</label>
                            <ValidationErrList :errors="err?.data?.points" />
                        </FloatLabel>
                    </div>
                </div>
            </div>
            <div v-else-if="fetchFailed">
                <div class="alert alert-danger">
                    Une erreur est survenue lors de la récupération du profil.
                </div>
            </div>
            <div v-else>
                Profil introuvable.
            </div>
        </template>
        <template #footer v-if="profile">
            <Button label="Sauvegarder" :disabled="submitting" @click="save" />
        </template>
    </Dialog>
</template>
<style>
.profile-edit-modal {
    min-width: min(100vw, 450px);
}
</style>
<style scoped>
.profile-edit-modal {
    & .-content {
        display: flex;
        flex-direction: column;
        gap: 16px;
    }

    & .-names {
        display: flex;
        width: 100%;
        gap: 8px;

        &>* {
            flex: 1;
        }

        & input {
            width: 100%;
        }
    }

    & .-gender {
        width: 100%;
    }

    & .-through {
        display: contents;
    }

    & .-admin {
        background: var(--gradient-admin);
        border-radius: 8px;

        & h3 {
            margin: 0;
            font-weight: bold;
            
            padding: 8px;
            padding-bottom: 4px;
            padding-left: 16px;
            border-radius: 8px 8px 0 0;
        }
    }

    & .-admin-fields {
            display: flex;
            flex-direction: column;
            gap: 16px;
            padding: 12px;
        }
}
</style>