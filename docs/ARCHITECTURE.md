# Explication des différents dossiers

- **docs**: Fichiers de documentation pratiques
- **src** : Fichiers sources du projet
  - **back** : Partie Backend - code Java de l'application
    - **assets** : Fichiers statiques (images, css, js, etc.) disponibles à la racine du serveur web (ex : `localhost:8080/image.jpeg` -> `static/image.jpeg`)
    - **java** : Fichiers Java
      - **fr/domotique** : Notre code
        - **api** : Toutes les "sections" de la partie API, toutes les requêtes possibles et leur fonctionnement
        - **site** : Toutes les "sections" de la partie site web, toutes les pages et leur fonctionnement
        - **data** : Classes permettant d'interagir avec la base de donnée (requêtes SQL, classes de données, etc.)
    - **logs** : Fichier de logs des exécutions précédentes du serveur
    - **resources** : Fichiers de configuration et documentation d'API
    - **views** : Templates/pages HTML au format [JTE](https://jte.gg/syntax)
  - **front** : Partie Frontend - code JavaScript avec Vue.js de l'application
- **src/test** : Le code des tests prédéfinis (pas important pour l'instant)