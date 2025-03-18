# Guide des Records Java avec MySQL (Vert.x)

## 1. Les bases des Records

```java
// Définition d'un record simple
record Utilisateur(int id, String nom, String email) {
    // Un record génère automatiquement:
    // - Constructeur
    // - Accesseurs (getters)
    // - equals(), hashCode() et toString()
}

// Utilisation de base
Utilisateur user = new Utilisateur(1, "Dupont", "jean@exemple.fr");
int id = user.id();           // Accès aux champs
String nom = user.nom();      // Utilise le nom du champ comme getter
```

## 2. Records pour les données SQL

### Mapping de base

```java
// Record représentant une ligne d'une table
record Produit(int id, String nom, double prix, String categorie) {}

// Récupérer un produit depuis une Row Vert.x
Future<Produit> getProduit(int id) {
    return db.preparedQuery("SELECT * FROM produits WHERE id = ?")
        .execute(Tuple.of(id))
        .map(rows -> {
            if (rows.size() == 0) return null;
            Row row = rows.iterator().next();
            // Créer un record à partir d'une Row
            return new Produit(
                row.getInteger("id"),
                row.getString("nom"),
                row.getDouble("prix"),
                row.getString("categorie")
            );
        });
}
```

### Méthode de factory dans un record

```java
record Commande(int id, String reference, double total, String statut) {
    // Méthode de factory pour créer depuis une Row
    static Commande fromRow(Row row) {
        if (row == null) return null;
        return new Commande(
            row.getInteger("id"),
            row.getString("reference"),
            row.getDouble("total"),
            row.getString("statut")
        );
    }
}

// Utilisation avec Vert.x
Future<Commande> getCommande(String ref) {
    return db.preparedQuery("SELECT * FROM commandes WHERE reference = ?")
        .execute(Tuple.of(ref))
        .map(rows -> rows.size() > 0 
            ? Commande.fromRow(rows.iterator().next()) 
            : null);
}
```

## 3. Records pour les requêtes et réponses

### Paramètres de requête

```java
// Record pour structurer les paramètres d'une requête
record FiltresProduit(String categorie, Double prixMin, Double prixMax) {
    // Construire dynamiquement une requête SQL
    String buildQuery() {
        StringBuilder query = new StringBuilder("SELECT * FROM produits WHERE 1=1");
        
        if (categorie != null) {
            query.append(" AND categorie = ?");
        }
        if (prixMin != null) {
            query.append(" AND prix >= ?");
        }
        if (prixMax != null) {
            query.append(" AND prix <= ?");
        }
        
        return query.toString();
    }
    
    // Créer un tuple de paramètres
    Tuple buildParams() {
        List<Object> params = new ArrayList<>();
        if (categorie != null) params.add(categorie);
        if (prixMin != null) params.add(prixMin);
        if (prixMax != null) params.add(prixMax);
        
        return Tuple.from(params);
    }
}

// Utilisation
Future<List<Produit>> rechercherProduits(FiltresProduit filtres) {
    return db.preparedQuery(filtres.buildQuery())
        .execute(filtres.buildParams())
        .map(rows -> {
            List<Produit> produits = new ArrayList<>();
            for (Row row : rows) {
                produits.add(new Produit(
                    row.getInteger("id"),
                    row.getString("nom"),
                    row.getDouble("prix"),
                    row.getString("categorie")
                ));
            }
            return produits;
        });
}
```

### Records imbriqués

```java
// Records imbriqués pour données complexes
record Adresse(String rue, String ville, String codePostal) {}

record Client(int id, String nom, Adresse adresse) {
    static Client fromRow(Row row) {
        return new Client(
            row.getInteger("id"),
            row.getString("nom"),
            new Adresse(
                row.getString("rue"),
                row.getString("ville"),
                row.getString("code_postal")
            )
        );
    }
}
```

## 4. Validation et logique métier

```java
record NouvelUtilisateur(String email, String motDePasse, String nom) {
    // Validateur compact avec validation dans le constructeur canonique
    NouvelUtilisateur {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email invalide");
        }
        if (motDePasse == null || motDePasse.length() < 8) {
            throw new IllegalArgumentException("Mot de passe trop court");
        }
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Nom requis");
        }
    }
    
    // Méthode pour insérer dans la base de données
    Future<Integer> insererDansBD(SqlClient db) {
        return db.preparedQuery(
                "INSERT INTO utilisateurs(email, mot_de_passe, nom) VALUES (?, ?, ?)")
            .execute(Tuple.of(email, motDePasse, nom))
            .map(rs -> rs.property(MySQLClient.LAST_INSERTED_ID));
    }
}
```

## 5. Records pour les jointures SQL

```java
// Records pour les résultats de jointures
record ProduitCategorie(int id, String nom, double prix, Categorie categorie) {
    record Categorie(int id, String nom, String description) {}
    
    static ProduitCategorie fromRow(Row row) {
        return new ProduitCategorie(
            row.getInteger("p.id"),
            row.getString("p.nom"),
            row.getDouble("p.prix"),
            new Categorie(
                row.getInteger("c.id"),
                row.getString("c.nom"),
                row.getString("c.description")
            )
        );
    }
}

// Requête avec jointure
Future<List<ProduitCategorie>> getProduitAvecCategories() {
    String query = """
        SELECT p.*, c.* 
        FROM produits p
        JOIN categories c ON p.categorie_id = c.id
        """;
        
    return db.query(query)
        .execute()
        .map(rows -> {
            List<ProduitCategorie> produits = new ArrayList<>();
            for (Row row : rows) {
                produits.add(ProduitCategorie.fromRow(row));
            }
            return produits;
        });
}
```

## 6. Records pour les transactions

```java
record TransactionCommande(
    Commande commande,
    List<LigneCommande> lignes
) {
    record Commande(String reference, int clientId, double total) {}
    record LigneCommande(String produitRef, int quantite, double prixUnitaire) {}
    
    // Méthode pour exécuter une transaction
    Future<Integer> executerTransaction(SqlClient client) {
        return client.withTransaction(conn -> {
            // 1. Insérer la commande
            return conn.preparedQuery(
                    "INSERT INTO commandes(reference, client_id, total) VALUES (?, ?, ?)")
                .execute(Tuple.of(
                    commande.reference(),
                    commande.clientId(),
                    commande.total()
                ))
                .map(rs -> rs.property(MySQLClient.LAST_INSERTED_ID))
                .compose(commandeId -> {
                    // 2. Insérer les lignes de commande
                    List<Future> futures = new ArrayList<>();
                    
                    for (LigneCommande ligne : lignes) {
                        futures.add(conn.preparedQuery(
                                "INSERT INTO lignes_commande(commande_id, produit_ref, quantite, prix_unitaire) VALUES (?, ?, ?, ?)")
                            .execute(Tuple.of(
                                commandeId,
                                ligne.produitRef(),
                                ligne.quantite(),
                                ligne.prixUnitaire()
                            ))
                        );
                    }
                    
                    // Attendre que toutes les lignes soient insérées
                    return CompositeFuture.all(futures).map(v -> commandeId);
                });
        });
    }
}
```

## 7. Records avec méthodes compactes

```java
// Records avec méthodes compactes et accesseurs personnalisés
record PrixProduit(double montantHT) {
    // Méthodes compactes pour des calculs dérivés
    double getTVA() { return montantHT * 0.2; }
    double getTTC() { return montantHT + getTVA(); }
    
    // Format personnalisé
    String formatTTC() {
        return String.format("%.2f €", getTTC());
    }
}

// Utilisation pour des statistiques
Future<Map<String, PrixProduit>> getTotalVentesParCategorie() {
    return db.query("""
            SELECT categorie, SUM(prix * quantite) as total_ht 
            FROM ventes 
            GROUP BY categorie
            """)
        .execute()
        .map(rows -> {
            Map<String, PrixProduit> totaux = new HashMap<>();
            for (Row row : rows) {
                String categorie = row.getString("categorie");
                double totalHT = row.getDouble("total_ht");
                totaux.put(categorie, new PrixProduit(totalHT));
            }
            return totaux;
        });
}
```

## 8. Bonnes pratiques

1. **Immutabilité**: Les records sont immuables - créez toujours de nouvelles instances plutôt que d'essayer de modifier les existantes
2. **Validation**: Placez la validation dans le constructeur canonique
3. **Factory Methods**: Utilisez des méthodes statiques pour créer des instances à partir de résultats SQL
4. **Lisibilité**: Préférez plusieurs petits records à un seul gros record
5. **Documentation**: Documentez l'objectif de chaque record et la signification de ses champs

## 9. Limitations

```java
// Les records ne peuvent pas:

// ❌ Étendre d'autres classes
// record MonRecord(int id) extends AutreClasse {}  // Interdit!

// ❌ Être étendus
// class MaClasse extends MonRecord {}  // Interdit!

// ❌ Déclarer des champs d'instance autres que ceux du constructeur
record Exemple(int a) {
    // ❌ int b;  // Interdit!
    
    // ✅ Mais les variables statiques sont autorisées
    static final String CONSTANTE = "valeur";
}
```

## Utilisation avec les Futures Vert.x

```java
// Exemple complet avec Futures
record ProduitDAO(SqlClient db) {
    Future<List<Produit>> rechercherProduits(String motCle) {
        return db.preparedQuery(
                "SELECT * FROM produits WHERE nom LIKE ? OR description LIKE ?")
            .execute(Tuple.of("%" + motCle + "%", "%" + motCle + "%"))
            .map(rows -> {
                List<Produit> resultats = new ArrayList<>();
                for (Row row : rows) {
                    resultats.add(new Produit(
                        row.getInteger("id"),
                        row.getString("nom"),
                        row.getDouble("prix"),
                        row.getString("categorie")
                    ));
                }
                return resultats;
            })
            .otherwise(err -> {
                System.err.println("Erreur de recherche: " + err.getMessage());
                return List.of(); // Liste vide en cas d'erreur
            });
    }
}
```

---

Ce guide couvre les utilisations les plus courantes des records Java avec MySQL via Vert.x SQL Client, en progressant des concepts de base aux cas d'utilisation plus avancés.