<script setup lang="js">
    import {Button} from "primevue";
    import {useRouter} from "vue-router";
    import {useAuthStore} from "@/stores/auth.js";
    import {ref} from "vue";

    const router = useRouter();
    const auth = useAuthStore();

    function logout() {
        auth.logout() // todo: prevent button spamming
    }
</script>

<template>
    <h1>Tableau de bord</h1>

    <p>Voilà voilà...</p>

    <Button @click="router.push({name: 'profile'})" label="Voir mon profil"></Button>
    <Button @click="logout" label="Déconnexion"></Button>
</template>

<style scoped>

</style>