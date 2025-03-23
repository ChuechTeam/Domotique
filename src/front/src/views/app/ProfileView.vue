<script setup lang="js">
    import {useAuthStore} from "@/stores/auth.js";
    import {storeToRefs} from "pinia";

    const auth = useAuthStore()

    // Use storeToRefs to have a "reactive" user so it changes when the user updates.
    const { user } = storeToRefs(auth)
</script>

<template>
<h1>Mon profil à moi (à améliorer pour inclure d'autres profils que soi)</h1>

    <p>Nom : {{ user.profile.firstName }}</p>
    <p>Prénom : {{ user.profile.lastName }}</p>
    <p>etc... (j'ai la flemme)</p>
</template>

<style scoped>

</style>