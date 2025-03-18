# Guide du SqlClient dans Vert.x pour MySQL

On suppose que `client` est un `SqlClient`, pour avoir le 
client SQL de l'application : `server.db()` (avec `server` donné au préalable).

## Requêtes simples

```java
// Exécution d'une requête simple
client
  .query("SELECT * FROM utilisateurs")
  .execute()
  .onSuccess(rows -> {
    // Traitement des résultats
    for (Row row : rows) {
      System.out.println("Utilisateur: " + row.getString("nom"));
    }
  })
  .onFailure(err -> {
    System.err.println("Erreur: " + err.getMessage());
  });
```

## Requêtes préparées avec paramètres

```java
// Requête préparée avec paramètres positionnels
client
  .preparedQuery("SELECT * FROM produits WHERE prix < ? AND categorie = ?")
  .execute(Tuple.of(20.0, "électronique"))
  .onSuccess(rows -> {
    System.out.println("Produits trouvés: " + rows.size());
  });

// Requête avec paramètres nommés
client
  .preparedQuery("SELECT * FROM commandes WHERE client_id = :clientId AND date > :date")
  .execute(new HashMap<String, Object>() {{
    put("clientId", 123);
    put("date", LocalDate.now().minusDays(30));
  }})
  .onSuccess(rows -> {
    System.out.println("Commandes récentes: " + rows.size());
  });
```

## Parcours et mapping des résultats

```java
client
  .preparedQuery("SELECT id, nom, email FROM utilisateurs WHERE actif = ?")
  .execute(Tuple.of(true))
  .map(rowSet -> {
    // Conversion du RowSet en List<Utilisateur>
    List<Utilisateur> utilisateurs = new ArrayList<>();
    for (Row row : rowSet) {
      utilisateurs.add(new Utilisateur(
        row.getInteger("id"),
        row.getString("nom"),
        row.getString("email")
      ));
    }
    return utilisateurs;
  })
  .onSuccess(utilisateurs -> {
    System.out.println("Utilisateurs actifs: " + utilisateurs.size());
  });
```

## Insertion de données

```java
// Insertion simple
client
  .preparedQuery("INSERT INTO produits (nom, prix, stock) VALUES (?, ?, ?)")
  .execute(Tuple.of("Smartphone XYZ", 499.99, 50))
  .onSuccess(result -> {
    // Récupération de l'ID généré
    long id = result.property(MySQLClient.LAST_INSERTED_ID);
    System.out.println("Produit ajouté avec ID: " + id);
  });

// Insertion avec récupération des clés générées
client
  .preparedQuery("INSERT INTO utilisateurs (nom, email) VALUES (?, ?)")
  .collecting(RowCollectors.rowCount())
  .execute(Tuple.of("Marie Dupont", "marie@example.com"))
  .onSuccess(count -> {
    System.out.println(count + " utilisateur(s) ajouté(s)");
  });
```

## Mise à jour de données

```java
client
  .preparedQuery("UPDATE produits SET prix = ?, stock = ? WHERE id = ?")
  .execute(Tuple.of(599.99, 45, 123))
  .onSuccess(result -> {
    int updated = result.rowCount();
    System.out.println(updated + " produit(s) mis à jour");
  });
```

## Transactions

```java
// Gestion d'une transaction complète
client
  .withTransaction(conn -> {
    // Première opération
    return conn.preparedQuery("INSERT INTO commandes (client_id, total) VALUES (?, ?)")
      .execute(Tuple.of(42, 199.99))
      .map(res -> res.property(MySQLClient.LAST_INSERTED_ID))
      .compose(commandeId -> {
        // Seconde opération avec le résultat de la première
        return conn.preparedQuery("INSERT INTO lignes_commande (commande_id, produit_id, quantite) VALUES (?, ?, ?)")
          .execute(Tuple.of(commandeId, 123, 2));
      });
  })
  .onSuccess(v -> {
    System.out.println("Transaction réussie");
  })
  .onFailure(err -> {
    System.err.println("Transaction annulée: " + err.getMessage());
  });
```

## Opérations par lots (batch)

```java
// Préparation d'un lot d'opérations
List<Tuple> produits = new ArrayList<>();
produits.add(Tuple.of("Produit 1", 29.99, "Catégorie A"));
produits.add(Tuple.of("Produit 2", 49.99, "Catégorie B"));
produits.add(Tuple.of("Produit 3", 19.99, "Catégorie A"));

// Exécution du lot
client
  .preparedQuery("INSERT INTO produits (nom, prix, categorie) VALUES (?, ?, ?)")
  .executeBatch(produits)
  .onSuccess(res -> {
    System.out.println("Nombre de produits insérés: " + res.size());
  });
```

## Requêtes paginées

```java
// Paramètres de pagination
int page = 2;
int elementsParPage = 10;
int offset = (page - 1) * elementsParPage;

client
  .preparedQuery("SELECT * FROM produits ORDER BY nom LIMIT ? OFFSET ?")
  .execute(Tuple.of(elementsParPage, offset))
  .onSuccess(rows -> {
    System.out.println("Page " + page + ": " + rows.size() + " produits");
  });
```

## Utilisation des streams

```java
// Traitement de grands ensembles de résultats via streaming
client
  .preparedQuery("SELECT * FROM logs WHERE date > ?")
  .createStream(50) // Taille du lot
  .handler(row -> {
    // Traitement de chaque ligne individuellement
    System.out.println("Log: " + row.getString("message"));
  })
  .endHandler(v -> {
    System.out.println("Tous les logs ont été traités");
  })
  .exceptionHandler(err -> {
    System.err.println("Erreur de streaming: " + err.getMessage());
  });
```

## Requêtes avancées

```java
// Requête avec jointure
client
  .preparedQuery("""
    SELECT u.id, u.nom, c.id as commande_id, c.total 
    FROM utilisateurs u 
    JOIN commandes c ON u.id = c.client_id 
    WHERE u.id = ?
  """)
  .execute(Tuple.of(42))
  .onSuccess(rows -> {
    // Traitement des résultats avec jointure
    Map<Integer, Utilisateur> utilisateurs = new HashMap<>();
    
    for (Row row : rows) {
      int userId = row.getInteger("id");
      if (!utilisateurs.containsKey(userId)) {
        utilisateurs.put(userId, new Utilisateur(
          userId, 
          row.getString("nom")
        ));
      }
      
      utilisateurs.get(userId).addCommande(new Commande(
        row.getInteger("commande_id"),
        row.getDouble("total")
      ));
    }
  });
```

## Fonctions utilitaires

```java
// Vérifier si une table existe
client
  .query("SHOW TABLES LIKE 'utilisateurs'")
  .execute()
  .map(rows -> rows.size() > 0) // true si la table existe
  .onSuccess(exists -> {
    if (exists) {
      System.out.println("La table existe");
    } else {
      System.out.println("La table n'existe pas");
    }
  });

// Obtenir le nombre total d'enregistrements
client
  .query("SELECT COUNT(*) as total FROM produits")
  .execute()
  .map(rows -> rows.iterator().next().getLong("total"))
  .onSuccess(total -> {
    System.out.println("Nombre total de produits: " + total);
  });
```

## Gestion des types spéciaux

```java
// Insertion avec date/heure
LocalDateTime maintenant = LocalDateTime.now();
client
  .preparedQuery("INSERT INTO evenements (titre, date_heure) VALUES (?, ?)")
  .execute(Tuple.of("Réunion importante", maintenant))
  .onSuccess(v -> System.out.println("Événement ajouté"));

// Gestion des JSON
JsonObject utilisateurJson = new JsonObject()
  .put("nom", "Jean Dupont")
  .put("preferences", new JsonObject()
    .put("theme", "sombre")
    .put("notifications", true)
  );

client
  .preparedQuery("INSERT INTO profils (utilisateur_id, donnees) VALUES (?, ?)")
  .execute(Tuple.of(42, utilisateurJson.encode()))
  .onSuccess(v -> System.out.println("Profil JSON enregistré"));
```

## Conseils et bonnes pratiques

1. Toujours libérer les ressources avec `close()` lorsque vous n'utilisez plus un client
2. Utiliser les requêtes préparées pour éviter les injections SQL
3. Gérer les transactions pour les opérations dépendantes
4. Limiter la taille du pool de connexions en fonction des besoins réels
5. Utiliser le streaming pour les grands ensembles de résultats
6. Préférer les paramètres nommés pour les requêtes complexes avec nombreux paramètres
7. Implémenter une couche DAO (Data Access Object) pour isoler la logique d'accès aux données