<script setup lang="js">

import {RouterView, RouterLink, useRouter} from "vue-router";
import {Menubar} from "primevue";
import {useAuthStore} from "@/stores/auth.js";
import {computed} from "vue";
import { useGlobalDialogsStore } from "@/stores/globalDialogs";
import { storeToRefs } from "pinia";

const auth = useAuthStore();
const router = useRouter();
const globalDialogs = useGlobalDialogsStore();

const { levelTooLow } = storeToRefs(globalDialogs);

function logout() {
    auth.logout()
}

const userLink = computed(() => "/profile/" + auth.user.profile.id);
</script>


<template>
    <div class="app-root">
        <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm px-4">
            <RouterLink class="navbar-brand fw-bold" to="/dashboard">Retraités Connectés</RouterLink>
            <div class="d-flex align-items-center gap-3">
                <RouterLink class="nav-link" to="/tech">Technologie</RouterLink>
                <RouterLink class="nav-link" to="/profiles">Profils</RouterLink>
                <RouterLink class="nav-link" to="/themes">Thèmes</RouterLink>
            </div>
            <RouterLink class="ms-auto user-pill" :to="userLink">
                {{ auth.user?.profile?.firstName + ' ' + auth.user?.profile?.lastName || 'Utilisateur' }}

            </RouterLink>
        </nav>
        <main class="content">
            <Suspense>
                <!-- Use $route.params as key so the component reloads when same route but different parameters (userId, deviceId...) -->
                <RouterView :key="$route.params" />
                <template #fallback>
                    <div class="spinner-border mx-auto" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                </template>
            </Suspense>
        </main>

        <Dialog v-model:visible="levelTooLow" modal header="Niveau insuffisant">
            <p>Votre niveau est insuffisant pour effectuer cette action !</p>
            <p>Pensez à participer plus pour atteindre le niveau supérieur !</p>
            <Button label="OK" @click="levelTooLow = false" class="mt-2" fluid />
        </Dialog>
    </div>
</template>

<style scoped>
.app-root {
    display: flex;
    flex-direction: column;
    height: 100vh;
}

.content {
    padding-top: 8px;
    flex: 1;
    overflow: auto;
}
.navbar-brand {
    font-size: 1.5rem;
    color: #2c3e50;
}
.nav-link {
    font-weight: 500;
    color: #333;
    text-decoration: none;
}
.nav-link:hover {
    color: #007bff;
}

.user-pill {
    display: flex;
    padding: 0.5rem 1rem;
    background-color: #f8f9fa;
    border-radius: 4px;
    font-size: 1rem;
    transition: all 0.3s ease;
    text-decoration: none;
    color: inherit;

    &:hover {
        background-color: #e2e6ea;
        cursor: pointer;
    }
    &.router-link-active {
        background-color: #007bff;
        color: white;
    }
}
</style>