# Projet domotique (nom temporaire)

## Prérequis
- JDK (Java Development Kit) 21 ou plus  
  **Commande Ubuntu :** `sudo apt install openjdk-21-jdk`
- MySQL 8.0 ou plus

## Première configuration

1. Cloner le dépôt dans un dossier au choix et s'y rendre  
   **Terminal (Linux & Windows) :** 
   ```bash
   git clone https://github.com/ChuechTeam/Domotique.git
   cd Domotique
   ```
   **IntelliJ :** File > New > Project from Version Control > GitHub
2. Télécharger toutes les dépendances du projet   
   **Terminal (Linux) :** `./gradlew build`     
   **Terminal (Windows) :** `gradlew.bat build`    
   **IntelliJ :** Onglet Gradle > Sync All Gradle Projects
3. Mettre l'utilisateur et le mot de passe MySQL dans le fichier de configuration `src/main/java/resources/application-dev-local.properties`
4. Lancer l'application !  
   **Terminal (Linux) :** `./gradlew bootRun`    
   **Terminal (Windows) :** `gradlew.bat bootRun`    
   **IntelliJ :** Bouton Run (flèche verte) 'Domotique (dev)'

## Documentation supplémentaire

Voir le dossier `docs/` du projet :

| Documentation                                                       | Description                                                                                          |
|---------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| **[Architecture](docs/ARCHITECTURE.md)**                            | Description de l'architecture du projet                                                              |
| **[Cookbook Database Entity](docs/COOKBOOK_DatabaseEntity.md)**     | Feuille pratique expliquant le système qui relie les classes Java aux tables MySQL (Spring Data JPA) |
| **[Cookbook Spring Controller](docs/COOKBOOK_SpringController.md)** | Feuille pratique pour créer des contrôleurs Spring (formulaires, templates, etc.)                    |
| **[Cookbook Templates](docs/COOKBOOK_Templates.md)**                | Feuille pratique pour écrire des templates JTE (Java Template Engine)                                |
| **[Help](docs/HELP.md)**                                            | Liens un peu au pif de documentation                                                                 |
