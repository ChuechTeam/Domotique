<template>
    <div class="font-['Montserrat'] bg-gray-50">
      <!-- Header -->
      <header class="bg-white shadow-md fixed w-full z-10">
        <div class="container mx-auto px-4 py-3 flex justify-between items-center">
          <div class="flex items-center">
            <!-- Logo animé -->
            <svg class="h-12 w-12 mr-2 logo-umbrella pulse" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
              <circle cx="50" cy="50" r="40" fill="#e63946" opacity="0.9" />
              <path d="M50,20 C60,30 80,30 80,45 L20,45 C20,30 40,30 50,20 Z" fill="#e63946" />
              <path d="M50,20 C60,30 80,30 80,45 L20,45 C20,30 40,30 50,20 Z" fill="#ffffff" opacity="0.3" />
              <path d="M48,45 L48,75 M52,45 L52,75" stroke="#333" stroke-width="3" />
              <circle cx="50" cy="55" r="3" fill="#333" />
              <circle cx="40" cy="60" r="8" fill="#e63946" opacity="0.8" />
              <circle cx="60" cy="60" r="8" fill="#e63946" opacity="0.8" />
              <path d="M36,60 C36,55 44,55 44,60" stroke="#333" stroke-width="2" fill="none" />
              <path d="M56,60 C56,55 64,55 64,60" stroke="#333" stroke-width="2" fill="none" />
            </svg>
            <div>
              <h1 class="text-2xl font-semibold text-gray-800">Retraités Connectés</h1>
              <p class="text-xs text-gray-500">Des solutions numériques pour seniors</p>
            </div>
          </div>
          <nav>
            <ul class="flex space-x-6">
              <li><a href="#" class="text-gray-600 hover:text-red-600 font-medium">Accueil</a></li>
              <li><a href="#" class="text-gray-600 hover:text-red-600 font-medium">Services</a></li>
              <li><a href="#" class="text-gray-600 hover:text-red-600 font-medium">EHPAD partenaires</a></li>
              <li><a href="#" class="text-gray-600 hover:text-red-600 font-medium">À propos</a></li>
              <li><a href="#" class="text-gray-600 hover:text-red-600 font-medium">Contact</a></li>
              <li><a href="#" class="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition">Connexion</a></li>
            </ul>
          </nav>
        </div>
      </header>
  
      <!-- Hero section -->
      <section class="pt-20 relative h-screen bg-cover bg-center" style="background-image: url('https://via.placeholder.com/1200x800')">
        <div class="absolute inset-0 bg-black opacity-50"></div>
        <div class="relative h-full flex items-center justify-center text-center">
          <div class="container px-6">
            <h1 class="text-5xl font-bold text-white mb-6 opacity-0 fade-in">Tout devient possible, ensemble.</h1>
            <p class="text-xl text-white mb-8 max-w-2xl mx-auto opacity-0 fade-in delay-200">
              Des outils connectés et une plateforme humaine pour faciliter la vie dans votre résidence.
            </p>
            <div class="flex justify-center space-x-4">
              <a href="#" class="bg-white text-red-600 font-semibold px-6 py-3 rounded-lg shadow-lg hover:bg-gray-100 transition opacity-0 fade-in delay-300">
                Pour les résidents
              </a>
              <a href="#" class="bg-red-600 text-white font-semibold px-6 py-3 rounded-lg shadow-lg hover:bg-red-700 transition opacity-0 fade-in delay-400">
                Pour l'équipe
              </a>
            </div>
          </div>
        </div>
      </section>
    </div>
  </template>
  
  <script setup>
  import { onMounted } from 'vue'
  
  onMounted(() => {
    document.querySelectorAll('.fade-in, .slide-in').forEach(el => {
      const observer = new IntersectionObserver(entries => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            entry.target.style.opacity = "1";
            observer.unobserve(entry.target);
          }
        });
      });
      observer.observe(el);
    });
  });
  </script>
  
  <style scoped>
  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  @keyframes pulse {
    0% {
      transform: scale(1);
    }
    50% {
      transform: scale(1.05);
    }
    100% {
      transform: scale(1);
    }
  }
  
  .fade-in {
    animation: fadeIn 1s ease forwards;
  }
  
  .pulse {
    animation: pulse 2s infinite;
  }
  
  .delay-200 {
    animation-delay: 0.2s;
  }
  
  .delay-300 {
    animation-delay: 0.3s;
  }
  
  .delay-400 {
    animation-delay: 0.4s;
  }
  </style>
  