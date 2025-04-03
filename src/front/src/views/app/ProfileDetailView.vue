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

const auth = useAuthStore();
const router = useRouter();

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
    profile = computed(() => user.value.profile);
} else {
    profile = ref(await api.users.findUser({ userId: props.userId }));
}

const genderIcon = computed(() => {
    if (!profile.value) return null;
    switch (profile.value.gender) {
        case "MALE":
            return "pi pi-mars";
        case "FEMALE":
            return "pi pi-venus";
        case "UNDISCLOSED":
            return "pi pi-asterisk";
    }
})

// Generate random background color based on user ID
function getAvatarColor(id) {
    // Simple hash function based on user ID
    const hash = Math.abs(id * 31) % 360;
    return `hsl(${hash}, 70%, 70%)`;
}

function profileUpdated(np) {
    console.log(profile);
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
            <header class="header">
                <Avatar :label="profile.firstName[0] + profile.lastName[0]"
                    :style="{ 'background-color': getAvatarColor(profile.id) }" shape="circle" class="avatar"></Avatar>
                <div class="infos">
                    <div class="full-name">{{ profile.firstName + ' ' + profile.lastName }}</div>
                    <div class="tags">
                        <Tag :value="roleLabels[profile.role]" severity="info" icon="pi pi-crown" />
                        <Tag :value="levelLabels[profile.level]" severity="success" icon="pi pi-trophy" class="ms-2" />
                        <Tag :value="genderLabels[profile.gender]" severity="secondary" :icon="genderIcon"
                            class="ms-2" style="--p-tag-secondary-background: rgb(222, 222, 222)" />
                    </div>
                    <LevelBar :value="profile.points" class="pe-2" />
                </div>
            </header>

            <!-- Profile Details -->
            <div class="devices prof-item">
                <h2>Appareils de {{  profile.firstName  }}</h2>
                <Suspense>
                    <ProfileDevices :user-id="userId" />
                    <template #fallback>
                        <div class="flex justify-content-center">
                            <ProgressSpinner />
                        </div>
                    </template>
                </Suspense>
            </div>

            <div class="rooms prof-item">
                <h2>Salles de {{ profile.firstName }}</h2>
            </div>

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
            <component :is="Component" @profile-update="profileUpdated" />
        </RouterView>
    </div>
</template>

<style scoped>
.avatar {
    width: 10rem;
    height: 10rem;
    font-size: 5rem;
}

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

    flex-wrap: wrap;
    justify-content: center;
}

.prof-item {
    padding-left: 16px;
    padding-right: 16px;
}

.infos {
    flex-grow: 1;
    min-width: min(80vw, 300px);
}

.full-name {
    font-size: 2.5em;
}

.avatar-placeholder {
    width: 120px;
    height: 120px;
    font-size: 2.5rem;
    font-weight: 500;
    margin: 0 auto;
    color: #6c757d;
    border: 2px solid #e9ecef;
}

.points-container {
    background-color: #f8f9fa;
    border-left: 4px solid #ffc107;
}

.card-header {
    border-bottom: 2px solid #f8f9fa;
}

@media (max-width: 768px) {
    .avatar-placeholder {
        width: 100px;
        height: 100px;
        font-size: 2rem;
    }

    .actions .p-buttongroup {
        flex-direction: column;
        width: 100%;
    }
}
</style>