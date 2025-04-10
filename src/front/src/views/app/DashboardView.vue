<template>
  <div class="scrollable-page">
    <div class="container-lg pt-5 pb-3">
      <header class="dashboard-header" v-if="user">
        <h1>Bienvenue, {{ user.profile.firstName }} 👋</h1>
        <p>Vous êtes connecté(e) en tant {{ user.profile.role === 'ADMIN' ? 'qu\'' : 'que ' }}<strong>{{
          roleLabels[user.profile.role] }}</strong>.</p>
      </header>

      <div v-else>
        <p>Erreur : utilisateur non connecté. </p>
      </div>

      <!-- Section profil -->
      <section class="profile-card mb-4" v-if="user">
        <h2 class="mb-4">Votre profil</h2>
        <ProfileHeader :profile="user.profile" />
        <Button fluid label="Voir mon profil" icon="pi pi-user"
          @click="router.push({ name: 'profile', params: { userId: user.profile.id } })" />
      </section>


      <!-- Section thématiques -->
      <section class="themes-section mb-4">
        <h2 class="mb-4">Thèmes</h2>
        <div class="theme-cards">
          <!-- Carte Santé -->
          <div class="theme-card health-card" @click="router.push({ name: 'health-theme' })">
            <div class="theme-header">
              <h3>Santé</h3>
              <i class="pi pi-arrow-right"></i>
            </div>
            <div class="theme-icon">
              <i class="pi pi-heart-fill"></i>
            </div>
            <p class="theme-description">Suivez vos données de santé et recevez des recommandations personnalisées</p>
            <Button label="Voir ma santé" @click.stop="router.push({ name: 'health-theme' })" />
          </div>

          <!-- Carte Énergie -->
          <div class="theme-card energy-card" @click="router.push({ name: 'energy-theme' })">
            <div class="theme-header">
              <h3>Énergie</h3>
              <i class="pi pi-arrow-right"></i>
            </div>
            <div class="theme-icon">
              <i class="pi pi-bolt"></i>
            </div>
            <p class="theme-description">Analysez la consommation énergétique et optimisez l'utilisation des appareils
            </p>
            <Button label="Voir la consommation d'électricité" @click.stop="router.push({ name: 'energy-theme' })" />
          </div>

          <!-- Carte Sport -->
          <div class="theme-card sport-card" @click="router.push({ name: 'sport-theme' })">
            <div class="theme-header">
              <h3>Sport</h3>
              <i class="pi pi-arrow-right"></i>
            </div>
            <div class="theme-icon">
              <i class="pi pi-chart-line"></i>
            </div>
            <p class="theme-description">Participez à des compétitions sportives et suivez votre classement</p>
            <Button label="Voir les classements" @click.stop="router.push({ name: 'sport-theme' })" />
          </div>
        </div>
      </section>

      <!-- Section objets connectés -->
      <section class="objects-section">
        <h2 class="mb-4">Vos appareils</h2>
        <ProfileDevices :user-id="user.profile.id" />
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/api'
import { roleLabels } from '@/labels'
import ProfileHeader from '../../components/ProfileHeader.vue'
import { useRouter } from 'vue-router'
import ProfileDevices from '@/components/ProfileDevices.vue'

const router = useRouter()

// ✅ Récupérer l'utilisateur connecté depuis le store
const auth = useAuthStore()
const user = auth.user

// Liste d'objets simulée
const objets = ref([
  {
    id: 1,
    nom: 'Thermostat',
    description: 'Température réglée à 22°C',
    etat: 'Actif'
  },
  {
    id: 2,
    nom: 'Lampe Chambre',
    description: 'Éteinte',
    etat: 'Inactif'
  }
])

const searchKeyword = ref('')
const searchFilter = ref('')

const filteredObjects = computed(() => {
  return objets.value.filter(obj =>
    (obj.nom.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      obj.description.toLowerCase().includes(searchKeyword.value.toLowerCase())) &&
    (searchFilter.value === '' || obj.etat === searchFilter.value)
  )
})

// ✅ Traquer la visite pour stats
api.userEvents.reportHomePageVisit()
</script>

<style scoped>
.dashboard-header {
  text-align: center;
  margin-bottom: 2rem;
}

.profile-card,
.objects-section,
.themes-section {
  background: #f4f4f4;
  border-radius: 12px;
  padding: 1.5rem;
}

.profile-card ul {
  list-style: none;
  padding: 0;
}

.profile-card li {
  margin-bottom: 0.5rem;
}

input,
select {
  padding: 10px;
  margin: 10px 0;
  width: 100%;
  max-width: 300px;
  border: 1px solid #ccc;
  border-radius: 6px;
}

.object-card {
  background: white;
  padding: 1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
}

/* Theme cards styling */
.theme-cards {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  justify-content: center;
}

.theme-card {
  flex: 1;
  min-width: 300px;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.theme-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.theme-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.theme-header h3 {
  font-size: 1.8rem;
  font-weight: 600;
  margin: 0;
}

.theme-header i {
  font-size: 1.5rem;
  transition: transform 0.2s;
}

.theme-card:hover .theme-header i {
  transform: translateX(5px);
}

.theme-icon {
  display: flex;
  justify-content: center;
  margin: 1rem 0;

  & i {
    font-size: 3rem;
  }
}

.theme-description {
  margin-bottom: 1.5rem;
  flex-grow: 1;
  line-height: 1.4;
}

/* Specific card styles */
.health-card {
  background: linear-gradient(145deg, rgb(236, 253, 245) 0%, rgb(200, 240, 220) 100%);
  border: 1px solid rgb(8, 150, 80, 0.3);
}

.health-card .theme-icon i {
  color: rgb(8, 150, 80);
}

.energy-card {
  background: linear-gradient(145deg, rgb(255, 244, 229) 0%, rgb(255, 224, 189) 100%);
  border: 1px solid rgb(172, 103, 14, 0.3);
}

.energy-card .theme-icon i {
  color: rgb(172, 103, 14);
}

.sport-card {
  background: linear-gradient(145deg, rgb(229, 237, 255) 0%, rgb(200, 210, 245) 100%);
  border: 1px solid rgb(38, 22, 126, 0.3);
}

.sport-card .theme-icon i {
  color: rgb(38, 22, 126);
}

/* Responsive */
@media (max-width: 768px) {
  .theme-cards {
    flex-direction: column;
    align-items: stretch;
  }

  .theme-card {
    max-width: none;
  }
}
</style>
