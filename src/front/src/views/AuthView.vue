<template>
  <div class="page-container">
  
    <div class="login-box">
      <h2>Se connecter </h2>

      <form @submit.prevent="handleLogin">
        <label for="identifier">Adresse email ou nom d'utilisateur</label>
        <input id="identifier" type="text" v-model="identifier" required />

        <label for="password">Mot de passe</label>
        <input id="password" type="password" v-model="password" required />

        <div v-if="error" class="error-msg">{{ error }}</div>

        <button type="submit" class="btn-primary" :disabled="loginProm != null">Continuer</button>
      </form>

      <div class="divider">ou</div>

      <p class="consent-text">
        En continuant, vous acceptez nos
        <a href="#">Conditions générales</a>,
        <a href="#">Conditions d’utilisation</a> et
        <a href="#">Politique de confidentialité</a>.
      </p>

      <p class="register-link">
      Nouveau ici ?
      <RouterLink class="regiterbutton" to="/register">Inscrivez-vous maintenant</RouterLink>
    </p>
    </div>
  </div>
</template>

<script setup>
import { findErrData } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const auth = useAuthStore()

const identifier = ref('')
const password = ref('')
const error = ref('')

const loginProm = ref(null)

const handleLogin = () => {
  loginProm.value = auth.login({
    email: identifier.value,
    password: password.value,
  }).then(e => {
    if (e) {
      error.value = e.message || "Une erreur est survenue."
    }
  }).finally(() => {
    loginProm.value = null
  })
}


</script>

<style scoped>
.page-container {
  font-family: 'Segoe UI', sans-serif;
  background-color: #fff;
  padding: 2rem;
  text-align: center;
}

.login-box {
  max-width: 420px;
  margin: 0 auto;
  padding: 2rem;
  border-radius: 6px;
}

h2 {
  font-size: 1.4rem;
  margin-bottom: 1.5rem;
  font-weight: 500;
  color: #111;
}

form {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 0.8rem;
}

label {
  text-align: left;
  font-size: 0.95rem;
  font-weight: 500;
  color: #333;
}

input {
  padding: 12px;
  font-size: 1rem;
  border: 1px solid #aaa;
  border-radius: 4px;
}

.btn-primary {
  background: black;
  color: white;
  padding: 12px;
  font-size: 1rem;
  font-weight: 600;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-primary:hover {
  background-color: #369f73;
}

.divider {
  margin: 2rem 0 1rem;
  position: relative;
  color: #888;
  font-size: 0.85rem;
}

.divider::before,
.divider::after {
  content: "";
  display: inline-block;
  height: 1px;
  width: 30%;
  background: #ccc;
  vertical-align: middle;
  margin: 0 0.5rem;
}

.consent-text {
  font-size: 0.8rem;
  color: #666;
  margin: 1rem 0;
}

.consent-text a {
  color: #111;
  text-decoration: underline;
}
.register-link {
  margin-top: 1rem;
  font-size: 14px;
}

.registerbutton {
  text-decoration: none;
  font-weight: bold;
}
.error-msg {
  color: #d93025;
  font-size: 0.9rem;
  margin-top: -0.5rem;
}
</style>