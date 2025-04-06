<template>
  <div class="container">
    <h2>Inscription</h2>
    <form @submit.prevent="validateForm">
      <!-- Champ Nom -->
      <div class="form-group">
        <label>Nom :</label>
        <input type="text" v-model="lastName" />
        <ValidationErrList :errors="errs.lastName" />
      </div>

      <!-- Champ Prénom -->
      <div class="form-group">
        <label>Prénom :</label>
        <input type="text" v-model="firstName" />
        <ValidationErrList :errors="errs.firstName" />
      </div>

      <!-- Champ Email -->
      <div class="form-group">
        <label>Email :</label>
        <input type="email" v-model="email" />
        <ValidationErrList :errors="errs.email" />
      </div>

      <!-- Sélection du Genre -->
      <div class="form-group">
        <label>Genre :</label>
        <select v-model="gender">
          <option value="" disabled>Selectionnez un Genre </option>
          <option v-for="g in genders" :key="g" :value="g">
            {{ genderLabels[g] }}
          </option>
        </select>
        <ValidationErrList :errors="errs.gender" />
      </div>

      <!-- Champ Mot de passe -->
      <div class="form-group">
        <label>Mot de passe :</label>
        <Password fluid v-model="password" toggleMask />
        <ValidationErrList :errors="errs.password" />
      </div>

      <!-- Invite Code -->
      <div class="form-group">
        <label>Code d'invitation :</label>
        <input v-model="inviteCode" />
        <ValidationErrList :errors="errs.inviteCode" />
      </div>

      <Button fluid type="submit">S'inscrire</Button>
    </form>

    <!-- Err message -->
    <div v-if="errMsg" class="text-danger">{{ errMsg }}</div>
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
      lastName: "",
      firstName: "",
      email: "",
      password: "",
      inviteCode: "",
      gender: "",
      errs: {},
      errMsg: ""
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

      if (this.inviteCode == "") {
        this.errs.inviteCode = ["Code d'invitation incorrect."];
        valid = false;
      }

      if (valid) {
        const err = await this.auth.register({
          email: this.email.trim(),
          gender: this.gender,
          firstName: this.firstName,
          lastName: this.lastName,
          role: this.selectedRole,
          password: this.password,
          inviteCode: this.inviteCode.trim(),
        })
        // If we have any error while registering, grab that error info
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
  display: block;
  /* Pour faire en sorte que les champs passent sous le label */

}

label {
  display: block;
  /* Pour que le label apparaisse au-dessus du champ */
  margin-bottom: 5px;
  /* Espacement entre le label et le champ */
}

input,
select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 20px;
  align-items: center;
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
