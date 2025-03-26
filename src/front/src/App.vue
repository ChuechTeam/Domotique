<script setup>
import {RouterView, useRouter} from 'vue-router'
import {useAuthStore} from "@/stores/auth"
import {storeToRefs} from "pinia";
import {watch} from "vue";

const router = useRouter()
const auth = useAuthStore()

// Begin loading the logged-in user in the background.
auth.fetchUser()

// Use storeToRefs to transform the store variables into "reactive" variables, so we can do stuff when that changes.
const {isLoggedIn} = storeToRefs(auth)

// When we, for some reason, get disconnected, and are in the "app" section; redirect to the home page.
// And, when we're connected and we're on the tour page (which is unusual), redirect to the dashboard.
watch(isLoggedIn, () => {
    if (isLoggedIn.value === false
        && router.currentRoute.value.matched.some(x => x.name === "app")) {
        router.push({name: "home"})
    } else if (isLoggedIn.value === true
        && router.currentRoute.value.matched.some(x => x.name === "tour")) {
        router.push({name: "dashboard"})
    }
})
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

    <RouterView />
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
