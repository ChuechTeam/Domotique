<template>
  <div class="page-container">
    <div class="login-box">
      <h2>Inscription</h2>

      <form @submit.prevent="validateForm">
        <!-- Champ Nom -->
        <label for="lastName">Nom</label>
        <input id="lastName" type="text" v-model="lastName" required />
        <ValidationErrList :errors="errs.lastName" />

        <!-- Champ Prénom -->
        <label for="firstName">Prénom</label>
        <input id="firstName" type="text" v-model="firstName" required />
        <ValidationErrList :errors="errs.firstName" />

        <!-- Champ Email -->
        <label for="email">Email</label>
        <input id="email" type="email" v-model="email" required />
        <ValidationErrList :errors="errs.email" />

        <!-- Sélection du Genre -->
        <label for="gender">Genre</label>
        <select id="gender" v-model="gender" required>
          <option value="" disabled>Selectionnez un Genre</option>
          <option v-for="g in genders" :key="g" :value="g">{{ genderLabels[g] }}</option>
        </select>
        <ValidationErrList :errors="errs.gender" />

        <!-- Champ Mot de passe -->
        <label for="password">Mot de passe</label>
        <Password id="password" fluid v-model="password" toggleMask />
        <ValidationErrList :errors="errs.password" />

        <!-- Invite Code -->
        <label for="inviteCode">Code d'invitation</label>
        <input id="inviteCode" v-model="inviteCode" />
        <ValidationErrList :errors="errs.inviteCode" />

        <!-- Bouton de soumission -->
        <button type="submit" class="btn-primary" :disabled="errs.length > 0">S'inscrire</button>
      </form>

      <div v-if="errMsg" class="error-msg">{{ errMsg }}</div>

      <div class="divider">ou</div>

      <p class="register-link">
        Déjà inscrit ?
        <RouterLink class="regiterbutton" to="/login">Se connecter</RouterLink>
      </p>
    </div>
  </div>
</template>

<script>
import ValidationErrList from '@/components/ValidationErrList.vue';
import { genderLabels, genders } from '@/labels';
import { useAuthStore } from '@/stores/auth';

export default {
  setup() {
    const auth = useAuthStore();
    return { auth, genderLabels, genders };
  },
  components: {
    ValidationErrList,
  },
  data() {
    return {
      lastName: '',
      firstName: '',
      email: '',
      password: '',
      inviteCode: '',
      gender: '',
      errs: {},
      errMsg: '',
    };
  },
  methods: {
    async validateForm() {
      this.errs = {}; // Réinitialisation des erreurs
      let valid = true;

      if (!this.lastName) {
        this.errs.lastName = ["Le nom est requis."];
        valid = false;
      }

      if (!this.firstName) {
        this.errs.firstName = ["Le prénom est requis."];
        valid = false;
      }

      if (!this.email || !this.email.includes("@")) {
        this.errs.email = ["Un email valide est requis."];
        valid = false;
      }

      if (!this.gender) {
        this.errs.gender = ["Veuillez sélectionner un genre."];
        valid = false;
      }

      if (!this.password || this.password.length < 6) {
        this.errs.password = ["Le mot de passe doit avoir au moins 6 caractères."];
        valid = false;
      }

      if (this.inviteCode === "") {
        this.errs.inviteCode = ["Code d'invitation incorrect."];
        valid = false;
      }

      if (valid) {
        const err = await this.auth.register({
          email: this.email.trim(),
          gender: this.gender,
          firstName: this.firstName,
          lastName: this.lastName,
          password: this.password,
          inviteCode: this.inviteCode.trim(),
        });
        
        if (err != null) {
          this.errs = err.data ?? {};
          this.errMsg = err.message ?? "Erreur lors de l'inscription !";
        }
      } else {
        this.errMsg = "Veuillez corriger les erreurs ci-dessus.";
      }
    },
  },
};
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

input, select {
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

.error-msg {
  color: #d93025;
  font-size: 0.9rem;
  margin-top: -0.5rem;
}

.register-link {
  margin-top: 1rem;
  font-size: 14px;
}

.registerbutton {
  text-decoration: none;
  font-weight: bold;
}

.registerbutton:hover {
  text-decoration: underline;
}
</style>
