# Par où commencer ?

L'architecture des dossiers du projet est définie dans le fichier [ARCHITECTURE.md](ARCHITECTURE.md).

Mais c'est plus pratique d'avoir une vue d'ensemble. Alors on va faire ça à la place.

## Vue d'ensemble

Le projet utilise le framework Vert.x qui nous permet d'avoir des **performances exceptionnelles**, 
sans nous imposer une tonne de fonctionnalités et de façons de faire. On choisit ce qu'on prend,
et **on organise le projet comme on veut**. 

Résultat : on ne se noie pas dans la complexité et l'**intégralité des systèmes est compréhensible en quelques heures** !

### Initialisation

Tout commence dans la classe [`MainVerticle`](/src/main/java/fr/domotique/MainVerticle.java), qui :
- récupère la configuration de l'application
- se connecte à la base de donnée MySQL
- configure les sessions, et les templates JTE
- crée des `RouterVerticle` pour accepter des requêtes du navigateur

Finalement, un objet `Server` est créé, qui contient tous les services importants, comme :
- `SqlClient` pour les requêtes SQL (`server.db()`)
- `Config` pour la configuration (`server.config()`)
- etc... (voir la javadoc)

### Définition des routes

La classe [`RouterVerticle`](/src/main/java/fr/domotique/RouterVerticle.java) accepte les requêtes HTTP et 
définit les **Routes** de l'application, à l'aide du **Routeur**.

Chaque **Route** lie une **URL** à une **fonction** qui traitera la requête. Exemple :
```java
router.get("/hello").handler(ctx -> ctx.end("Salut"));
```

Comme on va pas mettre tout le site dans une classe, les routes sont découpées en **Sections**.

### Sections

Une **Section** contient un ensemble de **Routes**.

Concrètement, une **Section** est une classe avec une fonction qui configure le **Routeur**, appelée `register`.

**Exemple de base**

```java
public class HelloSection extends Section {
    public HelloSection(Server server) {
        super(server);
    }
    
    public void register(Router router) {
        // Lier la FONCTION myPage à la ROUTE /my-page    (HTTP GET)
        router.get("/my-page").respond(this::myPage);
        
        // LIER la FONCTION info à la ROUTE /info    (HTTP GET)
        router.get("/info").respond(this::info);
    }
    
    // Fonction qui retourne un Future<Buffer>
    // ---> Affiche une page HTML (ou un fichier)
    Future<Buffer> myPage(RouterContext ctx) {
        // Utilise le template myPage.jte avec le nom "Soupe"
        return view(ctx, "myPage.jte", viewArg("name", "Soupe"));
    }
    
    // Fonction qui retourne un Future<...> avec un objet quelconque
    // ---> Retourne un objet JSON
    Future<SomeObject> info(RoutingContext ctx) {
        // Retourne la valeur { "a": 1, "b": 2 }
        return Future.succeededFuture(new SomeObject(1, 2));
    }
    
    // Un tuple simple
    record SomeObject(int a, int b) {}
}
```

Puis, pour activer la **Section**, il suffit de se rendre sur [`RouterVerticle`](/src/main/java/fr/domotique/RouterVerticle.java) et de modifier la fonction `allSections` :

```java
Section[] allSections() {
    return new Section[] {
            // ... sections précédentes ...
            new HelloSection(server),
    };
}
```

Et voilà !

### Templates JTE

Il est possible d'ajouter des pages HTML agrémentées de Java avec le format JTE dans le dossier `src/main/views`.

La syntaxe de JTE est expliquée sur [la fiche pratique](COOKBOOK_Templates.md).

Pour faire le rendu d'un template, on utilise la fonction `view` : 
```java
Future<Buffer> dashboard(RoutingContext ctx){
   // Render the views/dashboard.jte template with arguments using varargs syntax.
   return view(ctx, "dashboard.jte",
       viewArg("user", "Alice"),
       viewArg("money", -300),
       viewArg("isAdmin", false)
   );
}
```

### Requêtes SQL

Les requêtes SQL sont effectuées à l'aide du `SqlClient` de Vert.x, disponible sur l'objet 
`Server` : `server.db()`.

L'utilisation du `SqlClient` est détaillée dans la [fiche pratique](COOKBOOK_VertxSql.md).

### Configuration

La configuration de l'application est définie dans plusieurs fichiers du dossier `src/main/resources/` :

- `config.properties` : configuration appliquée systématiquement
- `config-dev.properties` : configuration appliquée en développement
- `config-dev-local.properties` : configuration appliquée en développement, pas envoyée sur GitHub (à utiliser pour les mdp de BDD)

Elle est au format properties, donc `clé=valeur`. Les clés possibles sont écrites sur la javadoc de `Config`.

### Frontend

Le frontend est rangé dans... le dossier `src/front`. 

Le serveur de développement Vue.js peut être : 
- lancé manuellement avec `npm run dev` (en étant dans le dossier `src/front`)
- lancé automatiquement en lançant le serveur Vert.x avec `./gradlew run`

Toutes les requêtes hors-API du serveur Vert.x sont redirigées vers le serveur de développement Vue.js.