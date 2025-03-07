# Contrôleurs

Ça permet de regrouper les requêtes et pages disponibles du site web.

Chaque fonction du contrôleur est liée à une URL (`@GetMapping` ou `@PostMapping`).

## Créer un contrôleur pour pages web

```java
@Controller
public class MyController {
    // <...>
}

```

## Créer un contrôleur pour API JSON

```java
@RestController
public class MyController {
    // <...>
}
```

## Utiliser la base de donnée (Repository) dans un contrôleur

```java
@Controller
public class MyController {
    private final UserRepository repo;
    
    // Ce constructeur est automatiquement utilisé par Spring (par magie en fait)
    public MyController(UserRepository repo) {
        this.repo = repo;
    }
    
    private void repositoryDemo() {
        // Je peux récupérer tous les utilisateurs de la BDD grâce au repository
        List<User> users = repo.findAll();
    }
}
```

## Afficher des messages dans la console

```java
@Controller
public class MyController {
    // Requiert : import org.slf4j.*;
    private static final Logger log = LoggerFactory.getLogger(MyController.class);
    
    private void loggingDemo(int x) {
        // Puis on peut utiliser n'importe quel sévérité avec des arguments
        // Les {} seront remplacés par chaque argument
        
        log.debug("Message de debug : {}", x);
        log.info("Message d'information : {}", x);
        log.warn("Message d'avertissement : {}", x);
        log.error("Message d'erreur : {}", x);
    }
}
```

# Contrôleur HTML

## Ajouter une page à un contrôleur

```java
@Controller
public class MyController {
    // Quand je fais la requête à l'URL http://localhost:8080/welcome
    // cette fonction sera utilisée
    @GetMapping("/welcome")
    public String welcome() {
        // Utilise le template src/jte/my-page.jte
        return "my-page";
    }
}
```

## Récupérer des paramètres d'URL (query)

Dans cet exemple, on récupère le `soupe` de l'URL `/my-page?name=soupe`.

```java
@Controller
public class MyController {
    @GetMapping("/welcome")
    public String welcome(@RequestParam String name) {
        log.info("name est : {}", name);
        return "my-page";
    }
}
```

## Récupérer des paramètres d'URL (route)

Dans cet exemple, on récupère le `soupe` de l'URL `/welcome/soupe`.

```java
@Controller
public class MyController {
    @GetMapping("/welcome/{name}")
    public String welcome(@PathVariable String name) {
        log.info("name est : {}", name);
        return "my-page";
    }
}
```

## Transférer des données au template

```java
@Controller
public class MyController {
    @GetMapping("/welcome")
    public String welcome(Model model) {
        // Ajout de l'attribute "name" avec la valeur "soupe"
        model.addAttribute("name", "soupe");
        return "my-page";
    }
}
```

Puis, dans le template `src/jte/my-page.jte` :
```html
<h1>Salut ${name} !</h1>
```

À noter que ça marche aussi avec des classes et des tableaux !

## Recevoir des données complexes du formulaire

```java
public class MyForm {
    private String name;
    private int age;
    
    // Getters et setters
}

@Controller
public class MyController {
    @PostMapping("/welcome")
    public String welcome(@ModelAttribute MyForm form) {
        log.info("Nom : {} ; Âge : {}", form.getName(), form.getAge());
        return "my-page";
    }
}
```

Dans le template HTML :
```html
<form action="/welcome" method="post">
    <!-- Attention à bien faire correspondre l'attribute name="" 
         au nom des attributs de la classe MyForm -->
    <input type="text" name="name" placeholder="Nom">
    <input type="number" name="age" placeholder="Âge">
    <button type="submit">Envoyer</button>
</form>
```

Ça marche avec n'importe quelle classe du moment qu'elle a 
- un constructeur public sans arguments
- des getters et setters pour chaque attribut  
  **Astuce :** Sur IntelliJ, utiliser le raccourci `Alt` + `Insert` pour générer automatiquement les getters et setters.

## Valider les données du formulaire

Source : [guide de Spring](https://spring.io/guides/gs/validating-form-input)

Liste complète des annotations de validation [sur la doc de Jakarta](https://jakarta.ee/learn/docs/jakartaee-tutorial/current/beanvalidation/bean-validation/bean-validation.html)

```java
public class MyForm {
    @Size(max=100, size="Le nom doit faire moins de 100 caractères")
    private String name;
    @Min(value=18, message="Vous devez avoir au moins 18 ans pour vous inscrire !")
    private int age;
    
    // Getters et setters
}

@Controller
public class MyController {
    @PostMapping("/register")
    public String register(@ModelAttribute @Valid MyForm form, BindingResult validResult) {
        if (validResult.hasErrors()) {
            // Oups y'a des problèmes
            return "register";
        }
        // Tout va bien !
        return "register-success";
    }
}
```