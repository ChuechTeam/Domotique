<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

import heroImage from '@/assets/hero-ehpad.jpg'
import staffImage from '@/assets/staff-care.jpg'
import drawing from '@/assets/drawing.jpg'
import sport from '@/assets/sport.jpg'
import gardening from '@/assets/gardening.jpg'
import music from '@/assets/music.jpg'
import actuEchec from '@/assets/actu_echec.jpg'
import solidarite from '@/assets/Solidarite.jpg'
import visite from '@/assets/Visite.jpg'

const isMenuOpen = ref(false)
const router = useRouter()

const scrollToSection = (id) => {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth' })
    isMenuOpen.value = false
  }
}

const activities = [
  { title: 'Atelier Dessin', description: 'Dans une ambiance calme et inspirante, les résidents laissent libre cours à leur imagination...', image: drawing },
  { title: 'Gym Douce', description: 'La gym douce est pensée pour respecter les capacités de chacun...', image: sport },
  { title: 'Jardinage', description: 'Mettre les mains dans la terre, arroser les fleurs...', image: gardening },
  { title: 'Atelier Musique', description: 'Dans une salle animée par des notes de piano ou des chants familiers...', image: music }
]

const newsCards = [
  {
    title: 'Semaine Bleue : le jeu d’échecs s’invite dans les maisons de retraite',
    image: actuEchec,
    link: 'https://www.capretraite.fr/blog/maisons-de-retraite/semaine-bleue-le-jeu-dechecs-sinvite-dans-les-maisons-de-retraite/'
  },
  {
    title: 'Brest : de nouvelles activités pour une retraite active et solidaire',
    image: solidarite,
    link: 'https://www.letelegramme.fr/finistere/brest-29200/centre-ville/a-brest-loisirs-solidarite-des-retraites-veut-lancer-de-nouvelles-activites-6767241.php'
  },
  {
    title: 'Nos résidents aiment avoir des jeunes qui s’intéressent à eux',
    image: visite,
    link: 'https://www.charentelibre.fr/charente/grand-cognac/nos-residents-aiment-avoir-des-jeunes-qui-s-interessent-a-eux-a-cognac-des-lyceens-se-rendent-en-ehpad-pour-apprendre-le-metier-23969333.php'
  }
]

const currentNewsIndex = ref(0)
const currentIndex = ref(0)
let intervalId = null
let newsInterval = null

function nextActivity() {
  currentIndex.value = (currentIndex.value + 1) % activities.length
}
function prevActivity() {
  currentIndex.value = (currentIndex.value - 1 + activities.length) % activities.length
}
function nextNews() {
  currentNewsIndex.value = (currentNewsIndex.value + 1) % newsCards.length
}
function prevNews() {
  currentNewsIndex.value = (currentNewsIndex.value - 1 + newsCards.length) % newsCards.length
}

onMounted(() => {
  intervalId = setInterval(nextActivity, 3000)
  newsInterval = setInterval(nextNews, 3000)
})
onUnmounted(() => {
  clearInterval(intervalId)
  clearInterval(newsInterval)
})
</script>

<template>
  <main class="home-container">
    <header class="header">
      <div class="title-center">Retraités Connectés</div>
      <div class="burger-menu" @click="isMenuOpen = !isMenuOpen">
        <span>&#9776;</span>
      </div>
      <div class="dropdown-menu" v-if="isMenuOpen">
        <button @click="scrollToSection('hero')">Accueil</button>
        <button @click="scrollToSection('activities')">Activités</button>
        <button @click="scrollToSection('actualites')">Actualités</button>
        <button @click="scrollToSection('rejoindre')">Nous rejoindre</button>
      </div>
    </header>

    <section id="hero" class="hero-section" :style="`background-image: url(${heroImage})`">
      <div class="overlay">
        <h1 class="title">Tout devient possible, ensemble.</h1>
        <p class="subtitle">
          Des outils connectés et une plateforme humaine pour faciliter la vie dans votre résidence.
        </p>
        <div class="buttons">
          <router-link to="/login" class="btn btn-light">Connexion</router-link>
          <router-link to="/register" class="btn btn-outline-light">Inscription</router-link>
        </div>
      </div>
    </section>

    <section id="activities" class="info-section">
      <h2>Nos Activités</h2>
      <div class="carousel">
        <div class="carousel-item" v-for="(activity, index) in activities" :key="index" :class="{ active: index === currentIndex }">
          <div class="image-container">
            <img :src="activity.image" :alt="activity.title" class="section-image" />
            <div class="caption">
              <h3>{{ activity.title }}</h3>
              <p>{{ activity.description }}</p>
            </div>
          </div>
        </div>
        <button class="arrow left" @click="prevActivity">&#8592;</button>
        <button class="arrow right" @click="nextActivity">&#8594;</button>
      </div>
    </section>

    <section id="actualites" class="info-section">
      <h2>Actualités</h2>
      <div class="news-carousel">
        <div class="news-item">
          <div class="image-container">
            <button class="arrow up" @click="prevNews">&#8593;</button>
            <img :src="newsCards[currentNewsIndex].image" :alt="newsCards[currentNewsIndex].title" class="section-image" />
            <button class="arrow down" @click="nextNews">&#8595;</button>
          </div>
          <h3>{{ newsCards[currentNewsIndex].title }}</h3>
          <a :href="newsCards[currentNewsIndex].link" target="_blank" class="news-link">Voir plus</a>
        </div>
      </div>
    </section>

    <section id="rejoindre" class="info-section">
      <h2>Nous rejoindre</h2>
      <img :src="staffImage" alt="Équipe" class="section-image" />
      <div class="contact-card">
        <p><strong>Email :</strong> retraitesconnectes@gmail.com</p>
        <p><strong>Téléphone :</strong> +33 06000000</p>
        <p><strong>Fixe :</strong> +33 05000000</p>
        <p><strong>Adresse :</strong> 5 rue Cergy, 95000 Cergy</p>
      </div>
    </section>
  </main>
</template>

<style scoped>
.home-container {
  font-family: Arial, sans-serif;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  padding: 1rem;
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
.title-center {
  font-size: 1.5rem;
  font-weight: bold;
  margin: auto;
  text-align: center;
}
.burger-menu {
  cursor: pointer;
  font-size: 1.5rem;
  position: absolute;
  left: 20px;
}
.dropdown-menu {
  position: absolute;
  top: 60px;
  left: 0;
  background: #f5f5f5;
  padding: 1rem;
  border-radius: 0 0 0 10px;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  z-index: 999;
}
.hero-section {
  width: 100vw;
  height: 100vh;
  background-size: cover;
  background-position: center;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}
.overlay {
  background-color: rgba(0, 0, 0, 0.6);
  padding: 3rem;
  text-align: center;
  color: white;
  border-radius: 8px;
}
.title {
  font-size: 3rem;
  font-weight: bold;
  margin-bottom: 1rem;
}
.subtitle {
  font-size: 1.3rem;
  max-width: 600px;
  margin: 0 auto 2rem auto;
}
.buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}
.btn {
  padding: 0.6rem 1.2rem;
  border-radius: 6px;
  cursor: pointer;
  text-decoration: none;
  font-weight: bold;
}
.btn-light {
  background-color: #fff;
  color: #333;
}
.btn-outline-light {
  background-color: transparent;
  border: 2px solid #fff;
  color: #fff;
}
.info-section {
  padding: 4rem 2rem;
  text-align: center;
  background: #f9f9f9;
}
.section-image {
  width: 100%;
  max-width: 800px;
  height: auto;
  border-radius: 10px;
  margin: auto;
  display: block;
}
.carousel {
  position: relative;
  max-width: 800px;
  margin: 2rem auto;
  overflow: hidden;
}
.carousel-item {
  display: none;
  transition: opacity 0.5s ease;
  text-align: center;
}
.carousel-item.active {
  display: block;
}
.image-container {
  position: relative;
}
.caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 1rem;
  border-radius: 0 0 10px 10px;
}
.arrow {
  position: absolute;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  padding: 0.5rem;
  cursor: pointer;
  font-size: 1.5rem;
  z-index: 10;
}
.arrow.left {
  top: 50%;
  left: 10px;
  transform: translateY(-50%);
}
.arrow.right {
  top: 50%;
  right: 10px;
  transform: translateY(-50%);
}
.arrow.up {
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
}
.arrow.down {
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
}
.news-carousel {
  max-width: 800px;
  margin: auto;
  text-align: center;
  position: relative;
}
.news-item {
  background: white;
  padding: 1rem;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 1rem;
  position: relative;
}
.news-link {
  color: #007BFF;
  text-decoration: underline;
  margin-top: 0.5rem;
  display: inline-block;
}
.contact-card {
  background: white;
  padding: 1.5rem;
  max-width: 600px;
  margin: 2rem auto;
  border-radius: 10px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}
</style>