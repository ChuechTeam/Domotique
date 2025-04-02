<script setup lang="js">
import { useAuthStore } from "@/stores/auth.js";
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import api from "@/api/index.js";
import { storeToRefs } from "pinia";
import { ButtonGroup } from "primevue";
import LoginLogs from "@/components/LoginLogs.vue";
import LevelBar from "@/components/LevelBar.vue";

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

function profileUpdated(profile) {
    if (!isCurrentUser.value) {
        profile.value = profile;
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
            <div class="card shadow-sm mb-4">
                <div class="card-body">
                    <div class="row align-items-center">
                        <div class="col-md-3 text-center mb-3 mb-md-0">
                            <div class="profile-avatar">
                                <div
                                    class="avatar-placeholder bg-light rounded-circle d-flex justify-content-center align-items-center">
                                    <span>{{ profile.firstName?.charAt(0) || '' }}{{ profile.lastName?.charAt(0) || ''
                                        }}</span>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-9">
                            <h1 class="fs-2 mb-2">{{ profile.firstName }} {{ profile.lastName }}</h1>
                            <div class="d-flex flex-wrap gap-2 mb-3">
                                <span class="badge bg-primary rounded-pill">{{ profile.gender }}</span>
                                <span :class="`badge bg-${getRoleColor(profile.role)} rounded-pill`">{{ profile.role
                                    }}</span>
                                <span :class="`badge bg-${getLevelColor(profile.level)} rounded-pill`">{{ profile.level
                                    }}</span>
                            </div>
                            <div class="points-container p-2 rounded d-inline-flex align-items-center">
                                <i class="bi bi-star-fill me-2"></i>
                                <span class="fw-bold">{{ profile.points }} points</span>
                            </div>

                            <LevelBar :value="profile.points" class="mt-2 pe-4" />
                        </div>
                    </div>
                </div>
            </div>

            <!-- Profile Details -->
            <div class="row">
                <!-- Personal Info -->
                <div class="col-12 col-md-6 mb-4">
                    <div class="card h-100 shadow-sm">
                        <div class="card-header bg-white">
                            <h2 class="fs-5 mb-0">Personal Information</h2>
                        </div>
                        <div class="card-body">
                            <ul class="list-group list-group-flush">
                                <li class="list-group-item d-flex justify-content-between">
                                    <span class="fw-medium">Full Name</span>
                                    <span>{{ profile.firstName }} {{ profile.lastName }}</span>
                                </li>
                                <li class="list-group-item d-flex justify-content-between">
                                    <span class="fw-medium">Gender</span>
                                    <span>{{ profile.gender }}</span>
                                </li>
                                <li class="list-group-item d-flex justify-content-between">
                                    <span class="fw-medium">ID</span>
                                    <span>{{ profile.id }}</span>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>

                <!-- User Status -->
                <div class="col-12 col-md-6 mb-4">
                    <div class="card h-100 shadow-sm">
                        <div class="card-header bg-white">
                            <h2 class="fs-5 mb-0">User Status</h2>
                        </div>
                        <div class="card-body">
                            <ul class="list-group list-group-flush">
                                <li class="list-group-item d-flex justify-content-between">
                                    <span class="fw-medium">Role</span>
                                    <span :class="`badge bg-${getRoleColor(profile.role)}`">{{ profile.role }}</span>
                                </li>
                                <li class="list-group-item d-flex justify-content-between">
                                    <span class="fw-medium">Tech Level</span>
                                    <span :class="`badge bg-${getLevelColor(profile.level)}`">{{ profile.level }}</span>
                                </li>
                                <li class="list-group-item d-flex justify-content-between align-items-center">
                                    <span class="fw-medium">Points</span>
                                    <div class="progress" style="width: 60%;">
                                        <div class="progress-bar" role="progressbar"
                                            :style="`width: ${Math.min(profile.points, 100)}%`"
                                            :aria-valuenow="profile.points" aria-valuemin="0" aria-valuemax="100">
                                            {{ profile.points }}
                                        </div>
                                    </div>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Action Buttons - Only show for current user -->
            <div v-if="canEdit" class="actions text-center">
                <ButtonGroup>
                    <Button fluid severity="secondary" icon="pi pi-user-edit"
                        @click="router.push({ name: 'profile-edit', params: { userId } })" label="Modifier le profil"></Button>
                    <Button fluid severity="secondary" icon="pi pi-key"
                        @click="router.push({ name: 'profile-creds', params: { userId } })" label="Changer le mot de passe"></Button>
                    <Button fluid severity="danger" icon="pi pi-trash"
                        @click="router.push({ name: 'profile-delete', params: { userId } })" label="Supprimer le compte"></Button>
                </ButtonGroup>
            </div>

            <div class="logs mt-2" v-if="auth.canAdminister">
                <h2>Historique de connexion</h2>
                <Suspense>
                    <LoginLogs :user-id="userId"/>
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
.profile-avatar {
    padding: 10px;
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