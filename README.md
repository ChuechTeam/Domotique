# Retraités connectés

Le site d'appareils connectés pour les retraités en EHPAD.

## Prérequis

**Vous êtes sur Linux ? Clonez le repo et lancez le script `./setup.sh` qui s'occupe de tout !**

- JDK (Java Development Kit) 23 ou plus  
  **Commandes Ubuntu (une ligne = une commande)**
  ```bash
  wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/adoptium.gpg > /dev/null
  echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
  sudo apt update && sudo apt install -y temurin-23-jdk
  ```
  **Windows** : [Télécharger le JDK Temurin 23](https://adoptium.net/fr/temurin/releases/?version=23&os=windows&arch=x64)
- Node.js 20 ou plus et NPM  
  **Commandes Ubuntu (une ligne = une commande)**
  ```bash
  sudo apt install -y npm
  ```
  **Windows** : [Télécharger Node.js](https://nodejs.org/en/download/current/)
- MySQL 8.0 ou plus

## Première configuration

**Vous êtes sur Linux ? Faites que l'étape 1 et lancez le script `./setup.sh` qui s'occupe de tout !**

1. Cloner le dépôt dans un dossier au choix et s'y rendre (important !)
   **Terminal (Linux & Windows) :**
   ```bash
   git clone https://github.com/ChuechTeam/Domotique.git
   cd Domotique
   ```
   **IntelliJ :** File > New > Project from Version Control > GitHub
2. Télécharger toutes les dépendances du projet Java  
   **Terminal (Linux) :** `./gradlew classes`  
   **Terminal (Windows) :** `gradlew.bat classes`  
   **IntelliJ :** Onglet Gradle > Sync All Gradle Projects
3. Télécharger toutes les dépendances du projet Frontend  
   **Terminal (Linux & Windows) :** 
   ```bash
   cd src/front
   npm install
   cd ../..
   ```
4. Mettre l'utilisateur et le mot de passe MySQL dans le fichier de configuration
   `src/main/java/resources/config-dev-local.properties`
5. Initialiser la base de données   
   **Terminal (Linux) :** `./gradlew updateDatabase` ou `./liquibase`     
   **Terminal (Windows) :** `gradlew.bat build` ou `./liquibase.bat`    
   **IntelliJ :** Onglet Gradle > Sync All Gradle Projects
6. Lancer l'application ! (Avec les données de base)  
   **Terminal (Linux) :** `./gradlew runSeed`    
   **Terminal (Windows) :** `gradlew.bat runSeed`    
   **IntelliJ :** Bouton Run (flèche verte) 'Run project'

Puis, ouvrez le navigateur à l'URL [http://localhost:7777](http://localhost:7777) pour avoir accès au site.

Il ne reste plus qu'à créer votre premier compte administrateur avec le code d'invitation "retraitons"

Pour lancer l'application, deux choix : 
- **Avec les données de base** : `./gradlew runSeed` ou `gradlew.bat runSeed` (réinitialise la base de donnée)
- **Sans les données de base** : `./gradlew run` ou `gradlew.bat run`

Avec ces données de base, tous les utilisateurs ont pour mot de passe "mot2passe". 
L'administrateur a pour e-mail "admin.boss@ehpad.fr"

## Que faire si je suis perdu ?

Regardez le fichier [LOST.md](docs/LOST.md) dans le dossier `docs/` du projet.

## Comment tester le backend ?

**Recommendation :** utiliser la documentation incluse `http://localhost:7777/api-docs`

## Documentation supplémentaire

Voir le dossier `docs/` du projet :

| Documentation                                                   | Description                                                          |
|-----------------------------------------------------------------|----------------------------------------------------------------------|
| **[Architecture](docs/ARCHITECTURE.md)**                        | Description de l'architecture du projet                              |
| **[Cookbook Vert.x Future](docs/COOKBOOK_VertxFuture.md)**      | Fiche pratique pour `Future<T>` de Vert.x                            |
| **[Cookbook Vert.x SQL](docs/COOKBOOK_VertxSql.md)**            | Fiche pratique pour `SqlClient` de Vert.x                            |
| **[Cookbook Java Records](docs/COOKBOOK_JavaRecords.md)**       | Fiche pratique pour les `records` Java                               |
| **[Cookbook Templates](docs/COOKBOOK_Templates.md)**            | Fiche pratique pour écrire des templates JTE (Java Template Engine)  |
| **[Cookbook Database Update](docs/COOKBOOK_DatabaseUpdate.md)** | Fiche pratique pour utiliser Liquibase                               |
| **[API JavaScript](docs/API_JavaScript.md)**                    | Guide pour utiliser le client API du JavaScript                      |
| **Script Javadoc** (`docs/javadoc`)                             | Script à lancer pour générer les javadocs accessibles sur navigateur |
| **[Help](docs/HELP.md)**                                        | Liens de documentation pris un peu au hasard                         |
