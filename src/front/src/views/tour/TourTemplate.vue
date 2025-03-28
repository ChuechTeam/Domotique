<script setup lang="js">
import {RouterView} from "vue-router";
</script>

<template>
    <div>
        <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm px-4">
            <RouterLink class="navbar-brand fw-bold" to="/">Retraités Connectés</RouterLink>
            <div class="ms-auto d-flex gap-3">
                <RouterLink class="nav-link" to="/">Accueil</RouterLink>
                <RouterLink class="nav-link" to="/register">Inscription</RouterLink>
                <RouterLink class="nav-link" to="/login">Connexion</RouterLink>
            </div>
        </nav>
        <RouterView/>
    </div>
</template>

<style scoped>
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
</style>