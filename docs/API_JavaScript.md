# Comment utiliser le client API JavaScript

## Pour récupérer une section

Toutes les sections de l'API sont présentes dans le module `api`.

```javascript
// Importer les classes nécessaires
import api from "@/api";

// J'accède à l'API des utilisateurs
const userApi = api.users;

// Puis à celui des appareils...
const deviceApi = api.devices;
```

## Utilisation de base avec Promises

Toutes les méthodes de l'API renvoient des Promises, ce qui vous permet d'utiliser `.then()` et `.catch()`.

```javascript
// Exemple: Récupérer le profil de l'utilisateur connecté
api.users.me()
  .then(user => {
    console.log("Utilisateur connecté:", user.profile.firstName);
    console.log("Points:", user.secret.points);
  })
  .catch(error => {
    console.error("Erreur lors de la récupération du profil:", error);
  });
```

## Utilisation avec async/await

Pour un code plus lisible, utilisez `async/await` pour gérer les appels à l'API.

```javascript
// Exemple: Connexion d'un utilisateur
async function loginUser(email, password) {
  try {
    const loginInput = {
      email: email,
      password: password
    };
    
    const user = await api.users.login({ loginInput });
    console.log("Connexion réussie pour:", user.profile.firstName);
    return user;
  } catch (error) {
    console.error("Échec de la connexion:", error);
    throw error; // Propagez l'erreur pour la gérer plus haut si nécessaire
  }
}

// Utilisation
api.users.login("utilisateur@exemple.com", "motdepasse")
  .then(user => {
    // Faire quelque chose avec l'utilisateur connecté
  })
  .catch(err => {
    // Gérer l'erreur
  });
```

## Gestion des erreurs avec ErrorResponse

Les erreurs de l'API contiennent des informations utiles dans l'objet `ErrorResponse`.

```javascript
// Approche complète pour gérer les erreurs
async function registerUser(userData) {
  try {
    const user = await api.users.register({ registerInput: userData });
    return user;
  } catch (error) {
    // Vérifier s'il y a une réponse d'erreur structurée
    if (error.response && error.response.errData) {
      const errorResponse = error.response.errData;
      
      // Accéder aux propriétés de l'ErrorResponse
      console.error("Code d'erreur:", errorResponse.code);
      console.error("Message:", errorResponse.message);
      
      // Utilisation des données supplémentaires (utiles pour les erreurs de validation)
      if (errorResponse.data) {
        console.error("Détails:", errorResponse.data);
      }
      
      // Vous pouvez adapter votre gestion selon le code d'erreur
      if (errorResponse.code === "EMAIL_ALREADY_EXISTS") {
        return { status: "error", message: "Cette adresse email est déjà utilisée" };
      }
    }
    
    // Erreur générique si rien de spécifique n'est trouvé
    throw error;
  }
}
```

## Exemples par fonctionnalité

### Récupérer les informations de l'utilisateur connecté

```javascript
async function getCurrentUser() {
  try {
    const user = await api.users.me();
    return {
      id: user.profile.id,
      name: `${user.profile.firstName} ${user.profile.lastName}`,
      email: user.secret.email,
      role: user.profile.role,
      points: user.secret.points,
      isEmailConfirmed: user.secret.emailConfirmed
    };
  } catch (error) {
    // Si l'erreur est 401, l'utilisateur n'est pas connecté
    // Si l'erreur est 401, l'utilisateur n'est pas connecté
    if (error.response && error.response.status === 401) {
    }
    throw error;
  }
}
```

### Inscription d'un nouvel utilisateur

```javascript
async function signUp(firstName, lastName, email, password, gender, role) {
  const registerInput = {
    firstName,
    lastName,
    email,
    password,
    gender, // MALE, FEMALE ou UNDISCLOSED
    role    // RESIDENT, CAREGIVER ou ADMIN
  };
  
  try {
    const newUser = await api.users.register({ registerInput });
    return newUser;
  } catch (error) {
    // Gérer spécifiquement les erreurs de validation (422)
    if (error.response && error.response.status === 422) {
      const errorResponse = error.response.errData;
      return {
        success: false,
        message: errorResponse.message,
        validationErrors: errorResponse.data
      };
    }
    throw error;
  }
}
```

### Connexion d'un utilisateur

```javascript
async function login(email, password) {
  const loginInput = { email, password };
  
  try {
    const user = await api.users.login({ loginInput });
    return {
      success: true,
      user: user
    };
  } catch (error) {
    if (error.response && error.response.status === 401) {
      return {
        success: false,
        message: "Email ou mot de passe incorrect"
      };
    }
    throw error;
  }
}
```

### Déconnexion

```javascript
async function logout() {
  try {
    await api.users.logout();
    return true;
  } catch (error) {
    console.error("Erreur lors de la déconnexion:", error);
    return false;
  }
}
```

### Rechercher un utilisateur par ID

```javascript
async function findUserById(userId) {
  try {
    const userProfile = await api.users.findUser({ userId });
    
    // Si la réponse est null (status 204), l'utilisateur n'existe pas
    if (!userProfile) {
      return { found: false };
    }
    
    return {
      found: true,
      user: userProfile
    };
  } catch (error) {
    console.error(`Erreur lors de la recherche de l'utilisateur ${userId}:`, error);
    throw error;
  }
}
```

## Techniques avancées

### Traitement en parallèle des requêtes

```javascript
async function loadDashboardData(userId) {
  try {
    // Exécuter plusieurs requêtes en parallèle
    const [currentUser, targetUser] = await Promise.all([
      api.users.me(),
      api.users.findUser({ userId })
    ]);
    
    return {
      currentUser,
      targetUser
    };
  } catch (error) {
    console.error("Erreur lors du chargement des données:", error);
    throw error;
  }
}
```

### Ajouter un intercepteur global pour les erreurs

Utiliser les `middleware` dans `api/index.ts`

## Bonnes pratiques

1. **Toujours gérer les erreurs** - Utilisez try/catch avec async/await ou .catch() avec Promises
2. **Centraliser la logique API** - Créez des services ou hooks qui encapsulent les appels API
3. **Validez les données** avant de les envoyer à l'API pour éviter les erreurs 422
4. **Utilisez les codes HTTP** pour déterminer le type d'erreur (401 pour auth, 422 pour validation, etc.)
5. **Implémentez une gestion de session** pour rediriger vers la page de connexion en cas d'expiration

## Référence rapide des codes d'erreur

- **401**: Non authentifié (token manquant ou expiré)
- **422**: Erreur de validation des données
- **204**: Ressource non trouvée (dans le cas de findUser)
- **500**: Erreur serveur interne

## Exemple d'application complète

```javascript
// userService.js
import { Configuration, UsersApi } from "@/api";

class UserService {
  constructor() {
    this.api = new UsersApi(new Configuration({ basePath: "" }));
  }
  
  async getCurrentUser() {
    try {
      return await this.api.me();
    } catch (error) {
      this.handleError(error);
      return null;
    }
  }
  
  async login(email, password) {
    try {
      return await this.api.login({ loginInput: { email, password } });
    } catch (error) {
      this.handleError(error);
      throw error;
    }
  }
  
  async register(userData) {
    try {
      return await this.api.register({ registerInput: userData });
    } catch (error) {
      this.handleError(error);
      throw error;
    }
  }
  
  async logout() {
    try {
      await this.api.logout();
      return true;
    } catch (error) {
      this.handleError(error);
      return false;
    }
  }
  
  handleError(error) {
    if (error.response) {
      const errorData = error.response.data;
      console.error(`[API Error ${error.response.status}]`, errorData?.message || 'Unknown error');
      
      // Vous pouvez implémenter une logique spécifique ici
      if (error.response.status === 401) {
        // Gérer l'expiration de session
      }
    } else {
      console.error('[API Error]', error.message);
    }
  }
}

export default new UserService();
```