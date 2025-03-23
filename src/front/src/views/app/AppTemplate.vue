<script setup lang="js">

import {RouterView, RouterLink, useRouter} from "vue-router";
import {Menubar} from "primevue";
import {useAuthStore} from "@/stores/auth.js";
import {computed} from "vue";

const auth = useAuthStore();
const router = useRouter();

function logout() {
    auth.logout()
}
</script>


<template>
    <div>
        <Menubar :model="[
            {
                label: 'Tableau de bord',
                route: '/dashboard',
            },
            {
                label: 'Profil',
                route: '/profile'
            },
            {
                label: 'Déconnexion',
                command: logout
            }
        ]">
            <!-- props here is for style mainly. remove it and its ugly. yeah it's not explained at all go figure -->
            <template #item="{ item, props, hasSubmenu }">
                <RouterLink v-if="item.route" v-slot="{ href, navigate }" :to="item.route" custom>
                    <a :href="href" v-bind="props.action" @click="navigate">
<!--                        <span :class="item.icon" />-->
                        <span>{{ item.label }}</span>
                    </a>
                </RouterLink>
                <a v-else :href="item.url" :target="item.target" v-bind="props.action">
<!--                    <span :class="item.icon" />-->
                    <span>{{ item.label }}</span>
                    <span v-if="hasSubmenu" class="pi pi-fw pi-angle-down" />
                </a>
            </template>

        </Menubar>
        <h1>Coucou (espace utilisateur)</h1>
        <RouterView/>
    </div>
</template>

<style scoped>

</style>