<script setup>
  import { RouterLink, RouterView } from 'vue-router'
  import HelloWorld from './components/HelloWorld.vue'
  import { onMounted, ref } from "vue"
  import { useAuthStore } from "@/stores/auth"
  import api from "@/api";
  import Button from "primevue/button";

  const auth = useAuthStore()
</script>

<template>
  <header>
    <img alt="Vue logo" class="logo" src="@/assets/logo.svg" width="125" height="125" />

    <div class="wrapper">
      <HelloWorld msg="You did it!" />
      <Button>Salut mec</Button>
      <div v-if="auth.loading">
        Chargement en cours...
      </div>
      <div v-else>
        <div v-if="auth.user">
          <p>Bonjour {{ auth.user.profile.firstName }}</p>
          <button @click="auth.logout()">Déconnexion</button>
        </div>
        <div v-else>
          <p>Faudrait être connecté non ?</p>
          <button @click="openLoginDialog">Connexion</button>
        </div>
      </div>

      <nav>
        <RouterLink to="/">Home</RouterLink>
        <RouterLink to="/about">About</RouterLink>
      </nav>
    </div>
  </header>
  <RouterView />
</template>