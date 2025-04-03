<script lang="ts" setup>
import api, { SearchUsersRequest, UserProfile } from '@/api';
import FullscreenSpinner from '@/components/FullscreenSpinner.vue';
import { useGuards } from '@/guards';
import { genderLabels, levelLabels, roleLabels } from '@/labels';
import { useAuthStore } from '@/stores/auth';
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

const guards = useGuards();
const router = useRouter();
const auth = useAuthStore();

const profiles = ref<UserProfile[]>([]);
const filters = ref({
    fullName: null as string | null,
});
const allUsersMode = computed({
    get: () => router.currentRoute.value.query?.allUsers === "true",
    set(v) {
        if (v) {
            router.push({ name: 'profiles', query: { allUsers: "true" } });
        } else {
            router.back(); // A bit dirty but hey it works!
        }
    }
});
const promise = ref<ReturnType<typeof api.users.searchUsers>>(null);
const searchTimer = ref<ReturnType<typeof setTimeout>>(null);

const loading = computed(() => promise.value != null || searchTimer.value != null);
const searchEmpty = computed(() => filters.value.fullName === null || filters.value.fullName.trim() === "");
const canSearchAllUsers = computed(() => auth.canAdminister);

function load() {
    if (!allUsersMode.value && searchEmpty.value) {
        profiles.value = [];
        return;
    }

    const apiInput: SearchUsersRequest = allUsersMode.value ? {} : {
        fullName: filters.value.fullName || undefined
    };

    let thisProm: any;
    async function fetchFromAPI() {
        try {
            const response = await api.users.searchUsers(apiInput);
            if (thisProm === promise.value) {
                profiles.value = response.profiles;
            }
        } catch (e) {
            console.error('Failed to load user profiles:', e);
        } finally {
            if (thisProm === promise.value) {
                promise.value = null;
            }
        }
    }
    thisProm = fetchFromAPI()
    promise.value = thisProm;
}

function viewProfile(profile: UserProfile) {
    router.push({ name: 'profile', params: { userId: profile.id } });
}

// Generate avatar initials from user's first and last name
function getInitials(profile: UserProfile): string {
    return (profile.firstName[0] + profile.lastName[0]).toUpperCase();
}

// Generate random background color based on user ID
function getAvatarColor(profile: UserProfile): string {
    // Simple hash function based on user ID
    const hash = Math.abs(profile.id * 31) % 360;
    return `hsl(${hash}, 70%, 70%)`;
}

// A fun hack to refresh Suspense-base components when their props change.
const routerKey = computed(() => {
    const func = router.currentRoute.value.meta?.generateKey as any
    if (func) {
        return func()
    } else {
        return ""
    }
});

watch(filters, () => {
    // Trigger refresh when changing filters.
    clearTimeout(searchTimer.value);
    searchTimer.value = setTimeout(() => {
        try {
            load();
        } finally {
            searchTimer.value = null;
        }
    }, 350);
}, { deep: true });

watch(allUsersMode, v => {
    filters.value.fullName = null;
    profiles.value = [];
    if (v) {
        promise.value = null;
        clearTimeout(searchTimer.value);

        load();
    }
});

load(); // Initial load
</script>

<template>
    <div class="profiles-view container-lg" v-if="router.currentRoute.value.name === 'profiles'">
        <h1 class="header">Profils</h1>

        <div class="search-section">
            <IconField class="flex-grow-1" v-if="!allUsersMode">
                <InputIcon class="pi pi-search" />
                <InputText v-model="filters.fullName" fluid placeholder="Rechercher un profil" />
            </IconField>
            <Button v-if="!allUsersMode && canSearchAllUsers" label="Tous les profils"
                @click="allUsersMode = true" icon="pi pi-users" />
            <Button v-if="allUsersMode" icon="pi pi-arrow-left" label="Retour" @click="allUsersMode = false" />
            <div v-if="allUsersMode" class="all-users-box">Consultation de tous les profils</div>
        </div>

        <div class="profiles-list">
            <div v-if="loading" class="loading-container">
                <FullscreenSpinner />
            </div>
            <div v-else-if="profiles.length === 0 && !searchEmpty" class="no-results">
                Aucun profil trouvé.
            </div>
            <div v-else-if="profiles.length === 0" class="text-center">
                <div class="pi pi-search mb-4" style="font-size: 6em;"></div>
                <div style="font-size: 2em;">Commencez à chercher un profil en tapant dans la barre de recherche
                    ci-dessus !</div>
            </div>
            <div v-else class="profile-cards">
                <div v-for="profile in profiles" :key="profile.id" class="profile-card" @click="viewProfile(profile)">
                    <Avatar class="avatar" :label="getInitials(profile)"
                        :style="{ backgroundColor: getAvatarColor(profile) }" />
                    <div class="profile-info">
                        <h3 class="profile-name">{{ profile.firstName }} {{ profile.lastName }}</h3>
                        <div class="profile-details">
                            <Badge :value="roleLabels[profile.role]" severity="info" class="profile-badge" />
                            <Badge :value="levelLabels[profile.level]" severity="success" class="profile-badge" />
                            <Badge :value="genderLabels[profile.gender]" severity="warning" class="profile-badge" />
                        </div>
                        <div class="profile-points">
                            <i class="pi pi-star" style="color: gold;"></i> {{ profile.points }} points
                        </div>
                    </div>
                    <div class="profile-actions">
                        <Button icon="pi pi-chevron-right" class="p-button-text" />
                    </div>
                </div>
            </div>
        </div>
    </div>
    <!-- Use routeKey as key so the component reloads when same route but different parameters (userId, deviceId...) -->
    <RouterView v-else :key="routerKey" />
</template>

<style lang="css" scoped>
.profiles-view {
    display: flex;
    flex-direction: column;
    padding: 1rem;
}

.header {
    margin: 2rem 0;
    text-align: center;
    font-size: 3rem;
}

.search-section {
    display: flex;
    gap: 1rem;
    margin-bottom: 1.5rem;
}

.profile-cards {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.all-users-box {
    border-radius: 4px;
    border: 1px solid rgba(0, 0, 0, 0.04);
    background-color: rgba(0, 0, 0, 0.02);
    color: #444;
    flex-grow: 1;
    justify-content: center;
    display: flex;
    align-items: center;
    min-height: 2.5em;
}

.profile-card {
    display: flex;
    align-items: center;
    padding: 1rem 1.5rem;
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;
}

.profile-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.avatar {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.2rem;
    font-weight: bold;
    color: white;
    margin-right: 1rem;
}

.profile-info {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
    flex: 1;
}

.profile-name {
    margin: 0;
    font-size: 1.2rem;
}

.profile-details {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
}

.profile-badge {
    font-size: 0.8rem;
}

.profile-points {
    display: flex;
    align-items: center;
    gap: 0.3rem;
    font-size: 0.9rem;
    color: #666;
}

.no-results {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 200px;
    color: #666;
    font-style: italic;
    background: rgba(0, 0, 0, 0.02);
    border-radius: 8px;
}

.loading-container {
    min-height: 200px;
    position: relative;
}

@media (max-width: 768px) {
    .search-section {
        flex-direction: column;
        align-items: stretch;
    }
}
</style>