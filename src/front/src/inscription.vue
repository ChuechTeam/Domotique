<template>
  <div class="page-container">
    <div class="login-box">
      <h2>S'inscrire</h2>
      <form @submit.prevent="validerFormulaire">
        <!-- Champ Nom -->
        <div class="form-group">
          <label>Nom :</label>
          <input type="text" v-model="nom" class="input" />
          <span v-if="erreurs.nom" class="error">{{ erreurs.nom }}</span>
        </div>

        <!-- Champ Prénom -->
        <div class="form-group">
          <label>Prénom :</label>
          <input type="text" v-model="prenom" class="input" />
          <span v-if="erreurs.prenom" class="error">{{ erreurs.prenom }}</span>
        </div>

        <!-- Champ Email -->
        <div class="form-group">
          <label>Email :</label>
          <input type="email" v-model="email" class="input" />
          <span v-if="erreurs.email" class="error">{{ erreurs.email }}</span>
        </div>

        <!-- Sélection du Genre -->
        <div class="form-group">
          <label>Genre :</label>
          <select v-model="selectedGenre" class="input">
            <option value="" disabled>Selectionnez un Genre</option>
            <option v-for="genre in genre" :key="genre" :value="genre">
              {{ genre }}
            </option>
          </select>
          <span v-if="erreurs.selectedGenre" class="error">{{ erreurs.selectedGenre }}</span>
        </div>

        <!-- Sélection du Rôle -->
        <div class="form-group">
          <label>Rôle :</label>
          <select v-model="selectedRole" class="input">
            <option value="" disabled>Choisissez un rôle</option>
            <option v-for="role in roles" :key="role" :value="role">
              {{ role }}
            </option>
          </select>
          <span v-if="erreurs.selectedRole" class="error">{{ erreurs.selectedRole }}</span>
        </div>

        <!-- Champ Mot de passe -->
        <div class="form-group">
          <label>Mot de passe :</label>
          <Password fluid v-model="motDePasse" toggleMask  />
          <span v-if="erreurs.motDePasse" class="error">{{ erreurs.motDePasse }}</span>
        </div>

        <!-- Mot de passe Admin (si Admin sélectionné) -->
        <div v-if="selectedRole === 'ADMIN'" class="form-group">
          <label>Mot de passe Administrateur :</label>
          <input type="password" v-model="motDePasseAdmin" class="input" />
          <span v-if="erreurs.motDePasseAdmin" class="error">{{ erreurs.motDePasseAdmin }}</span>
        </div>

        <Button fluid type="submit" class="btn-primary">S'inscrire</Button>
      </form>

      <p class="login-link">
        Vous avez déjà un compte ?
        <a href="/login" class="loginbutton">Connectez-vous ici</a>
      </p>
      <p v-if="messageSucces" class="success">{{ messageSucces }}</p>
    </div>
  </div>
</template>

<script>
import { useAuthStore } from './stores/auth';

export default {
  setup() {
    const auth = useAuthStore();
    return { auth };
  },
  data() {
    return {
      nom: "",
      prenom: "",
      email: "",
      selectedGenre: "",
      selectedRole: "",
      motDePasse: "",
      motDePasseAdmin: "",
      erreurs: {},
      messageSucces: "",
      genre: ["MALE", "FEMALE", "UNDISCLOSED"],
      roles: ["RESIDENT", "ADMIN", "CAREGIVER"],
    };
  },
  methods: {
    async validerFormulaire() {
      this.erreurs = {};
      let valide = true;

      if (!this.nom) {
        this.erreurs.nom = "Le nom est requis.";
        valide = false;
      }

      if (!this.prenom) {
        this.erreurs.prenom = "Le prénom est requis.";
        valide = false;
      }

      if (!this.email || !this.email.includes("@")) {
        this.erreurs.email = "Un email valide est requis.";
        valide = false;
      }

      if (!this.selectedGenre) {
        this.erreurs.selectedGenre = "Veuillez sélectionner un genre.";
        valide = false;
      }

      if (!this.selectedRole) {
        this.erreurs.selectedRole = "Veuillez sélectionner un rôle.";
        valide = false;
      }

      if (!this.motDePasse || this.motDePasse.length < 6) {
        this.erreurs.motDePasse = "Le mot de passe doit avoir au moins 6 caractères.";
        valide = false;
      }

      if (
        this.selectedRole === "Admin" &&
        (!this.motDePasseAdmin || this.motDePasseAdmin !== "admin123")
      ) {
        this.erreurs.motDePasseAdmin = "Mot de passe Admin incorrect.";
        valide = false;
      }

      if (valide) {
        this.messageSucces = "Inscription réussie !";
        await this.auth.register({
          email: this.email,
          gender: this.selectedGenre,
          firstName: this.prenom,
          lastName: this.nom,
          role: this.selectedRole,
          password: this.motDePasse,
        });
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
.form-group {
  margin-bottom: 15px;
  text-align: left;
}

input {
  padding: 12px;
  font-size: 1rem;
  border: 1px solid #aaa;
  border-radius: 4px;
}

.input,
input,
select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 8px;
  box-sizing: border-box;
  font-size: 14px;
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


.login-link {
  margin-top: 1rem;
  font-size: 14px;
}

.loginbutton {
    text-decoration: none;
  font-weight: bold;
}

.loginbutton:hover {
  text-decoration: underline;
}

.error {
  color: red;
  font-size: 12px;
  margin-top: 4px;
  display: block;
}

.success {
  color: green;
  font-size: 14px;
  margin-top: 10px;
}
</style>
