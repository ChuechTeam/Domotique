<template>
  <div class="container">
    <h2>Inscription</h2>
    <form @submit.prevent="validerFormulaire">
      <!-- Champ Nom -->
      <div class="form-group">
        <label>Nom :</label>
        <input type="text" v-model="nom" />
        <span v-if="erreurs.nom" class="error">{{ erreurs.nom }}</span>
      </div>

      <!-- Champ Prénom -->
      <div class="form-group">
        <label>Prénom :</label>
        <input type="text" v-model="prenom" />
        <span v-if="erreurs.prenom" class="error">{{ erreurs.prenom }}</span>
      </div>

      <!-- Champ Email -->
      <div class="form-group">
        <label>Email :</label>
        <input type="email" v-model="email" />
        <span v-if="erreurs.email" class="error">{{ erreurs.email }}</span>
      </div>
      
      <!-- Sélection du Genre -->
      <div class="form-group">
        <label>Genre :</label>
        <select v-model="selectedRole">
          <option value="" disabled>Selectionnez un Genre </option>
          <option v-for="genre in genre" :key="genre" :value="genre">
            {{ genre }}
          </option>
        </select>
        <span v-if="erreurs.selectedGenre" class="error">{{ erreurs.selectedGenre }}</span>
      </div>

      <!-- Sélection du Rôle -->
      <div class="form-group">
        <label>Rôle :</label>
        <select v-model="selectedRole">
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
        <input type="password" v-model="motDePasse" />
        <span v-if="erreurs.motDePasse" class="error">{{ erreurs.motDePasse }}</span>
      </div>

      <!-- Mot de passe Admin (Affiché seulement si "Admin" est sélectionné) -->
      <div v-if="selectedRole === 'Admin'" class="form-group">
        <label>Mot de passe Administrateur :</label>
        <input type="password" v-model="motDePasseAdmin" />
        <span v-if="erreurs.motDePasseAdmin" class="error">{{ erreurs.motDePasseAdmin }}</span>
      </div>

      <button type="submit">S'inscrire</button>
    </form>

    <!-- Message de succès -->
    <p v-if="messageSucces" class="success">{{ messageSucces }}</p>
  </div>
</template>

<script>
export default {
  data() {
    return {
      nom: "",
      prenom: "",
      email: "",
      selectedRole: "",
      motDePasse: "",
      motDePasseAdmin: "", 
      erreurs: {},
      messageSucces: "",
      genre:["Homme","Femme","Autre"],
      roles: ["Résident", "Admin", "Soignant"] 
    };
  },
  methods: {
    validerFormulaire() {
      this.erreurs = {}; // Réinitialisation des erreurs
      let valide = true;

      // Validation des champs
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

      if (!this.selectedRole) {
        this.erreurs.selectedRole = "Veuillez sélectionner un rôle.";
        valide = false;
      }

      if (!this.motDePasse || this.motDePasse.length < 6) {
        this.erreurs.motDePasse = "Le mot de passe doit avoir au moins 6 caractères.";
        valide = false;
      }

      if (this.selectedRole === "Admin" && (!this.motDePasseAdmin || this.motDePasseAdmin !== "admin123")) {
        this.erreurs.motDePasseAdmin = "Mot de passe Admin incorrect.";
        valide = false;
      }

      if (valide) {
        this.messageSucces = "Inscription réussie !";
        console.log("Données envoyées :", {
          nom: this.nom,
          prenom: this.prenom,
          email: this.email,
          role: this.selectedRole,
          motDePasse: this.motDePasse,
          motDePasseAdmin: this.motDePasseAdmin
        });
       setTimeout(() => {
      this.$router.push("/login");
  },  1500); // redirige après 1,5 seconde
      }
    },
  },
};
</script>

<style scoped>
.container {
  max-width: 400px;
  margin: auto;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 10px;
  box-shadow: 2px 2px 10px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 15px;
  display: block;       /* Pour faire en sorte que les champs passent sous le label */

}
label {
  display: block;       /* Pour que le label apparaisse au-dessus du champ */
  margin-bottom: 5px;   /* Espacement entre le label et le champ */
}
input, select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 20px;
  align-items: center;
}

button {
  background: #42b983;
  color: white;
  padding: 10px;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  width: 50%;

}

button:hover {
  background: #35495e;
}

.error {
  color: red;
  font-size: 12px;
}

.success {
  color: green;
  font-size: 14px;
}
</style>
