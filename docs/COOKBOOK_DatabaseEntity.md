### `@Entity`
Définit une classe comme une entité persistante.

```java
@Entity
public class Utilisateur {
    // ...
}
```

### `@Table`
Spécifie la table de la base de données associée à l'entité.

```java
@Entity
@Table(name = "utilisateurs", 
       schema = "app_schema",
       uniqueConstraints = @UniqueConstraint(columnNames = {"email", "telephone"}))
public class Utilisateur {
    // ...
}
```

### `@Id`
Marque un champ comme clé primaire.

```java
@Id
private Long id;
```

### `@GeneratedValue`
Configuration pour la génération automatique de valeurs de clé primaire.

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO INCREMENT de MySQL
private Long id;
```

### `@Column`
Spécifie les propriétés de la colonne associée à un champ.

```java
@Column(name = "email_address", 
        length = 100, 
        nullable = false, 
        unique = true)
private String email;
```

### `@Transient`
Indique qu'un champ ne doit pas être enregistré dans la base de donnée.

```java
@Transient
private String motDePasseTemporaire;
```

## Annotations de relations

### `@OneToOne`
Ajoute une clé étrangère entre deux entités.

```java
@OneToOne(cascade = CascadeType.ALL)
@JoinColumn(name = "adresse_id", referencedColumnName = "id")
private Adresse adresse;
```

### `@OneToMany`
Relation un-à-plusieurs entre deux entités.

```java
@OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Commande> commandes = new ArrayList<>();
```

### `@ManyToOne`
Relation plusieurs-à-un entre deux entités.

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "departement_id")
private Departement departement;
```

### `@ManyToMany`
Relation plusieurs-à-plusieurs entre deux entités.

```java
@ManyToMany
@JoinTable(
    name = "utilisateur_role", 
    joinColumns = @JoinColumn(name = "utilisateur_id"), 
    inverseJoinColumns = @JoinColumn(name = "role_id")
)
private Set<Role> roles = new HashSet<>();
```

## Autres annotations utiles

### `@Embedded` et `@Embeddable`
Pour intégrer des classes entières dans une entité.

```java
@Embeddable
public class Adresse {
    private String rue;
    private String ville;
    private String codePostal;
    // ...
}

@Entity
public class Utilisateur {
    @Embedded
    private Adresse adresse;
    // ...
}
```

### `@ElementCollection`
Pour les collections de types basiques ou d'objets embarqués.

```java
@Entity
public class Utilisateur {
    @ElementCollection
    @CollectionTable(name = "telephone_utilisateur", joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Column(name = "numero_telephone")
    private Set<String> telephones = new HashSet<>();
    // ...
}
```

### `@Lob`
Pour les objets volumineux (comme les images ou les documents).

```java
@Lob
private byte[] photo;
```

### `@Enumerated`
Pour persister des valeurs d'énumération.

```java
@Enumerated(EnumType.STRING)
private StatutCommande statut;
```

### `@Formula`
Spécifique à Hibernate, pour des colonnes calculées.

```java
@Formula("(select count(*) from commande c where c.utilisateur_id = id)")
private int nombreCommandes;
```

### `@NaturalId`
Pour définir un identifiant naturel unique (spécifique à Hibernate).

```java
@NaturalId
@Column(nullable = false, unique = true)
private String email;
```

### `@CreationTimestamp` et `@UpdateTimestamp`
Annotations Hibernate pour l'horodatage automatique.

```java
@CreationTimestamp
private LocalDateTime dateCreation;

@UpdateTimestamp
private LocalDateTime dateMiseAJour;
```