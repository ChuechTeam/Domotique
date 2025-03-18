# Comment utiliser les templates ?

1. Écrire les templates en format `.jte` dans le dossier `src/main/views/` du projet.  
   **Exemple :** `src/main/views/superPage.jte`

# Comment écrire un template ?

Après avoir créé un fichier `.jte` dans le dossier `src/main/views/`, on peut **écrire
du HTML agrémenté de Java**, avec plusieurs façons de le faire. 

**Fortement recommendé** : Installer le [plugin JTE](https://plugins.jetbrains.com/plugin/14521-jte)
sur IntelliJ.

## Importations et paramètres

Pour recevoir des objets Java du contrôleur, on utilise :

- `@import` : Importe des classes Java (comme `import` en Java justement)
- `@param` : Définit les paramètres reçus depuis le contrôleur

```html
<!-- Importations de classes Java -->
@import fr.domotique.model.User
@import java.util.List

<!-- viewArg("utilisateur", monUser); -->
@param User utilisateur
<!-- viewArg("messages", maListe); -->
@param List<String> messages
<!-- viewArg("estConnecte", true); -->
@param boolean estConnecte = false <!-- paramètre optionnel avec valeur par défaut -->
```

Pour cet exemple, la section peut ressembler à :
```java
// Dans une classe qui hérite de Section
Future<Void> discussions(RoutingContext context) {
    // Création d'un utilisateur
    User utilisateur = new User("Jean", "jean@example.com");
     
    // Création d'une liste de messages
    List<String> messages = List.of("Bonjour", "Bonsoir", "Au revoir");

    // Rendu du template avec les paramètres
    return view(context, "monTemplate", 
        viewArg("utilisateur", utilisateur), // -> @param User utilisateur
        viewArg("messages", messages),       // -> @param List<String> messages
        viewArg("estConnecte", true)         // -> @param boolean estConnecte
    );
}
```

## Affichage des données

Pour afficher des données dans un template, on utilise `${}`:

```html
@import fr.domotique.data.User
@param User utilisateur

<p>Age: ${utilisateur.getAge()}</p> <!-- Affichage d'un int -->
<p>Actif: ${utilisateur.isActif()}</p> <!-- Affichage d'un booléen -->
<p>Email: ${utilisateur.getEmail()}</p> <!-- Affichage d'un string -->
```
## Structures de contrôle

### Conditions (if/else)

```html
@if(utilisateur.isAdmin())
    <p>Bienvenue, administrateur!</p>
@elseif(utilisateur.isPremium())
    <p>Bienvenue, utilisateur premium!</p>
@else
    <p>Bienvenue!</p>
@endif
```

### Boucles

```html
<!-- Boucle for classique -->
@for(Produit produit : panier.getProduits())
    <li>${produit.getNom()} - ${produit.getPrix()}€</li>
@endfor

<!-- Avec var (Java 10+) -->
@for(var produit : panier.getProduits())
    <li>${produit.getNom()} - ${produit.getPrix()}€</li>
@endfor

<!-- Boucle for avec compteur -->
@for(int i = 0; i < produits.size(); i++)
    <li>Produit numéro ${i+1} : ${produits.get(i).getNom()}</li>
@endfor
```

### Boucles avec état vide

```html
@for(var produit : panier.getProduits())
    <li>${produit.nom}</li>
@else
    <li>Votre panier est vide</li>
@endfor
```

## Commentaires

Les commentaires ne sont pas inclus dans le HTML généré !

```html
<%-- Ceci est un commentaire qui ne sera pas rendu --%>
<p>Contenu visible</p>
```

## Appels de templates

Les templates peuvent être réutilisés et appelés depuis d'autres templates:

Dans le fichier **components/produit.jte** :
```html
@import fr.domotique.model.Produit
@param Produit produit

<div class="produit">
    <h3>${produit.nom}</h3>
    <p>${produit.description}</p>
    <span class="prix">${produit.prix}€</span>
</div>
```

Pour l'appeler depuis un autre template:

```html
@import fr.domotique.model.Panier
@param Panier panier

<div class="liste-produits">
    @for(var produit : panier.getProduits())
        @template.components.produit(produit = produit)
    @endfor
</div>
```

### Paramètres par défaut

```html
<!-- Dans components/alert.jte -->
@param String message
@param String type = "info"

<div class="alert alert-${type}">
    ${message}
</div>

<!-- Utilisation -->
@template.components.alert(message = "Opération réussie")
@template.components.alert(message = "Attention!", type = "warning")
```

## Blocs de contenu

Le type `Content` permet de passer des blocs de contenu à d'autres templates:

```html
<!-- Dans layouts/base.jte -->
@import gg.jte.Content
@param String titre
@param Content contenu
@param Content footer = null

<!DOCTYPE html>
<html>
<head>
    <title>${titre}</title>
</head>
<body>
    <main>
        ${contenu}
    </main>
    
    @if(footer != null)
    <footer>
        ${footer}
    </footer>
    @endif
</body>
</html>
```

Utilisation:

```html
@template.layouts.base(
    titre = "Ma page d'accueil",
    contenu = @`
        <h1>Bienvenue sur notre site</h1>
        <p>Découvrez nos produits et services.</p>
    `,
    footer = @`
        <p>&copy; 2023 - Ma Société</p>
    `
)
```

## Variables locales

Déclarez des variables locales avec `!{}`:

```html
!{var total = 0;}

@for(var produit : panier.getProduits())
    !{total += produit.getPrix();}
    <li>${produit.nom}</li>
@endfor

<p>Total: ${total}€</p>
```

## Échappement HTML automatique

Par défaut, JTE n'échappe pas le HTML. 

C'est-à-dire qu'une variable avec la valeur `<b>Salut</b>` sera affichée en gras.

Il faut donc faire gaffe à bien échapper le contenu des utilisateurs, **sinon quelqu'un pourrait mettre du HTML malicieux**.

```html
<!-- Échappé -->
${gg.jte.html.HtmlUtils.escape(utilisateur.description)}

<!-- Non-échappé (comportement par défaut) -->
${utilisateur.descriptionHtml}
```

## Inclure du code Java brut

```html
!{
    String greeting;
    if (utilisateur.isVip()) {
        greeting = "Bienvenue cher VIP!";
    } else {
        greeting = "Bienvenue!";
    }
}

<p>${greeting}</p>
```