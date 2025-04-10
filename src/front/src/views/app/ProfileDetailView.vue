<script setup lang="js">
import { useAuthStore } from "@/stores/auth.js";
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import api from "@/api/index.js";
import { storeToRefs } from "pinia";
import { ButtonGroup } from "primevue";
import LoginLogs from "@/components/LoginLogs.vue";
import LevelBar from "@/components/LevelBar.vue";
import { genderLabels, levelLabels, roleLabels } from "@/labels";
import ProfileDevices from "@/components/ProfileDevices.vue";
import { usePrefsStore } from "@/stores/prefs";
import ProfileHeader from '../../components/ProfileHeader.vue'

const auth = useAuthStore();
const router = useRouter();
const prefs = usePrefsStore();

const props = defineProps({
    userId: {
        type: String,
        required: true
    }
});
const userId = parseInt(props.userId);
const isCurrentUser = ref(userId === auth.userId);
const canEdit = computed(() => isCurrentUser.value || auth.canAdminister);

let profile;
if (isCurrentUser.value) {
    const { user } = storeToRefs(auth);
    await auth.fetchUser();

    profile = computed(() => user.value.profile);
    await api.userEvents.reportOwnProfileCheck();
} else {
    profile = ref(await api.users.findUser({ userId: props.userId }));
    await api.userEvents.reportOtherProfilesCheck();
}

const settingsDialogOpen = ref(false);
const prefsRefs = storeToRefs(prefs);

function profileUpdated(np) {
    if (!isCurrentUser.value) {
        profile.value = np;
    }
}

// Role and Level colors
const getRoleColor = (role) => {
    const colors = {
        ADMIN: 'danger',
        USER: 'primary',
        VOLUNTEER: 'success',
        // Add other roles as needed
    };
    return colors[role] || 'secondary';
};

const getLevelColor = (level) => {
    const colors = {
        BEGINNER: 'info',
        INTERMEDIATE: 'warning',
        ADVANCED: 'success',
        // Add other levels as needed
    };
    return colors[level] || 'secondary';
};
</script>

<template>
    <div class="container py-3">
        <div v-if="!profile" class="alert alert-warning">
            Ce profil n'existe pas.
        </div>

        <div v-else class="profile-container">
            <!-- Profile Header -->
            <ProfileHeader :profile="profile" class="p-sm-3 p-md-5" />

            <!-- Profile Details -->
            <div class="devices prof-item">
                <h2 class="mb-3">Appareils de {{ profile.firstName }}</h2>
                <Suspense>
                    <ProfileDevices :user-id="userId" />
                    <template #fallback>
                        <div class="flex justify-content-center">
                            <ProgressSpinner />
                        </div>
                    </template>
                </Suspense>
            </div>

            <!-- <div class="rooms prof-item">
                <h2>Salles de {{ profile.firstName }}</h2>
            </div> (TODO) -->

            <!-- Action Buttons - Only show for current user -->
            <div v-if="canEdit" class="actions text-center">
                <ButtonGroup>
                    <Button fluid severity="secondary" icon="pi pi-user-edit"
                        @click="router.push({ name: 'profile-edit', params: { userId } })"
                        label="Modifier le profil"></Button>
                    <Button fluid severity="secondary" icon="pi pi-key"
                        @click="router.push({ name: 'profile-creds', params: { userId } })"
                        label="Changer le mot de passe"></Button>
                    <Button fluid severity="danger" icon="pi pi-trash"
                        @click="router.push({ name: 'profile-delete', params: { userId } })"
                        label="Supprimer le compte"></Button>
                </ButtonGroup>
            </div>

            <div class="logs prof-item mt-2" v-if="auth.canAdminister">
                <h2>Historique de connexion</h2>
                <Suspense>
                    <LoginLogs :user-id="userId" />
                    <template #fallback>
                        <ProgressSpinner />
                    </template>
                </Suspense>
            </div>
        </div>
        <RouterView v-slot="{ Component }">
            <component :is="Component" @profile-update="profileUpdated" @profile-delete="router.back()" />
        </RouterView>
    </div>
</template>

<style scoped>
.profile-container {
    border-radius: 16px;
    background-color: #fbfbfb;
    padding-bottom: 16px;
    /* overflow: hidden; */
}

.header {
    background-color: #f6f6f6;
    /* background-color: white; */
    /* box-shadow: 0 0 12px rgba(0, 0, 0, 0.1); */

    padding: 40px;

    border-bottom: none;

    border-radius: 16px 16px 0 0;

    margin-bottom: 1rem;

    display: flex;

    gap: 40px;

    justify-content: center;
}

.prof-item {
    padding-left: 16px;
    padding-right: 16px;
}

.infos {
    flex-grow: 1;

    display: grid;

    grid-template: "name actions" "level level";
    align-items: center;
}

@media (max-width: 1024px) {
    .infos {
        grid-template: "name" "level" "actions";
    }

    .details-actions {
        width: 100%;
        margin-top: 1rem;
    }

    .logout {
        flex-grow: 1;
    }

    .header {
        flex-direction: column;

        & .avatar {
            align-self: center;
        }
    }
    
    .actions .p-buttongroup {
        flex-direction: column;
        width: 100%;
    }
}
.card-header {
    border-bottom: 2px solid #f8f9fa;
}
</style>