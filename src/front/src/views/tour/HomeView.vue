<script setup>
import { ref } from 'vue'
import imageRetraite from '@/assets/image-retraite.jpeg'

const searchKeyword = ref('')
const searchLocation = ref('')
</script>

<template>
  <main class="home-container">
    <!-- Section image de fond -->
    <section class="hero-section" :style="`background-image: url(${imageRetraite})`">
      <div class="overlay">
        <h1 class="title">Tout devient possible, ensemble.</h1>
        <p class="subtitle">
          Des outils connectés et une plateforme humaine pour faciliter la vie dans votre résidence.
        </p>
        <div class="buttons">
          <router-link to="/resident" class="btn btn-light">Pour les résidents</router-link>
          <router-link to="/staff" class="btn btn-outline-light">Pour l’équipe</router-link>
        </div>
      </div>
    </section>

    <!-- Barre de recherche -->
    <section class="search-bar">
      <input v-model="searchKeyword" placeholder="Mot-clé, activité..." />
      <input v-model="searchLocation" placeholder="Étage, zone, etc." />
      <button class="btn btn-danger">Rechercher</button>
    </section>
  </main>
</template>

<style scoped>
.home-container {
  width: 100%;
  margin: 0;
  padding: 0;
}

.hero-section {
  width: 100vw;
  height: 100vh;
  background-size: cover;
  background-position: center;
  position: relative;
}

.overlay {
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5); /* filtre foncé */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  color: white;
  text-align: center;
}

.title {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 1rem;
}

.subtitle {
  font-size: 1.2rem;
  margin-bottom: 2rem;
  max-width: 600px;
}

.buttons {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

/* Barre de recherche */
.search-bar {
  padding: 2rem;
  text-align: center;
  background: #f8f9fa;
}

.search-bar input {
  margin: 0.5rem;
  padding: 0.6rem;
  width: 200px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.search-bar button {
  margin-top: 1rem;
}
</style>
