<template>
    <div class="dashboard-container">
      <header class="dashboard-header">
        <h1>Bienvenue, {{ user.prenom }} 👋</h1>
        <p>Vous êtes connecté(e) en tant que <strong>{{ user.role }}</strong>.</p>
      </header>
  
      <!-- Section profil -->
      <section class="profile-card">
        <h2>Votre profil</h2>
        <ul>
          <li><strong>Nom :</strong> {{ user.nom }}</li>
          <li><strong>Prénom :</strong> {{ user.prenom }}</li>
          <li><strong>Âge :</strong> {{ user.age }} ans</li>
          <li><strong>Rôle :</strong> {{ user.role }}</li>
        </ul>
      </section>
  
      <!-- Section objets connectés -->
      <section class="objects-section">
        <h2>Objets connectés</h2>
        <input v-model="searchKeyword" placeholder="Nom ou type d’objet..." />
        <select v-model="searchFilter">
          <option value="">Filtrer par état</option>
          <option>Actif</option>
          <option>Inactif</option>
        </select>
  
        <div class="object-card" v-for="obj in filteredObjects" :key="obj.id">
          <h3>{{ obj.nom }}</h3>
          <p>{{ obj.description }}</p>
          <p><strong>État :</strong> {{ obj.etat }}</p>
        </div>
      </section>
    </div>
  </template>
  
  <script setup>
  import { ref, computed } from 'vue'
  
  const user = {
    nom: 'Ben Omar',
    prenom: 'Saad',
    age: 23,
    role: 'Fils'
  }
  
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
  </script>
  
  <style scoped>
  .dashboard-container {
    max-width: 900px;
    margin: 3rem auto;
    padding: 2rem;
    font-family: 'Segoe UI', sans-serif;
  }
  
  .dashboard-header {
    text-align: center;
    margin-bottom: 2rem;
  }
  
  .profile-card, .objects-section {
    background: #f4f4f4;
    border-radius: 12px;
    padding: 1.5rem;
    margin-bottom: 2rem;
  }
  
  .profile-card ul {
    list-style: none;
    padding: 0;
  }
  
  .profile-card li {
    margin-bottom: 0.5rem;
  }
  
  input, select {
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
    margin-bottom: 1rem;
  }
  </style>
  