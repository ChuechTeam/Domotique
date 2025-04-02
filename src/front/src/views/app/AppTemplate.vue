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

const userLink = computed(() => "/profiles/" + auth.user.profile.id);
const userLabel = computed(() => auth.user.profile.firstName[0] + auth.user.profile.lastName[0]);
</script>


<template>
    <div class="app-root">
        <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm px-4">
            <RouterLink class="navbar-brand fw-bold" to="/dashboard"><span>Retraités Connectés</span></RouterLink>
            <div class="links">
                <RouterLink class="nav-link" to="/dashboard"><span class="pi pi-home"></span><span class="nav-label">Accueil</span></RouterLink>
                <RouterLink class="nav-link" to="/tech"><span class="pi pi-microchip"></span><span class="nav-label">Technologie</span></RouterLink>
                <RouterLink class="nav-link" to="/profiles"><span class="pi pi-user"></span><span class="nav-label">Profils</span></RouterLink>
                <RouterLink class="nav-link" to="/themes"><span class="pi pi-box"></span><span class="nav-label">Thèmes</span></RouterLink>
            </div>
            <Chip class="ms-auto user-icon">
                <Avatar :label="userLabel" size="medium" shape="circle" />
                <RouterLink :to="userLink" style="text-decoration: none; color: unset;">{{ auth.user.profile.firstName  }}</RouterLink>
            </Chip>
        </nav>
        <main class="content">
            <Suspense>
                <RouterView />
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

    scrollbar-gutter: stable both-edges;
    scrollbar-width: thin;

    &:has(> .no-overflow) {
        overflow: hidden;
    }
}
.navbar-brand {
    font-size: 1.5rem;
    color: #2c3e50;
}
.links {
    display: flex;
    justify-content: stretch;
    flex: 1;
    max-width: 640px;
    margin: 0 auto;
    gap: 8px;
}
.nav-link {
    font-weight: 500;
    color: #333;
    text-decoration: none;
    padding: 8px;
    border-radius: 8px;
    flex: 1;
    text-align: center;

    &:hover {
        color: #416d3d;
    }

    & .pi {
        margin-right: 8px;
        vertical-align: baseline;
    }

    &.router-link-active {
        background-color: #1b8014;
        color: white;
        
        &:hover {
            color: white;
        }  
    }
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

.mobile-only {
    display: none;
}

@media (max-width: 768px) {
    .app-root {
        flex-direction: column-reverse;
    }
    .navbar-brand {
        display: none;
    }
    .mobile-only {
        display: block;
    }
    .navbar-brand .nav-label {
        display: none;
    }
    .navbar {
        box-shadow: 0px 0px 16px 2px rgb(0,0,0, 10%) !important;
    }
    .nav-link {
        display: flex;
        flex-direction: column;
        align-items: center;
    }
    .user-icon {
        display: none;
    }
    .navbar .pi {
        display: block !important; /* hacky */
        font-size: 2rem;
        margin: 0.5rem 0;
    }
}
</style>