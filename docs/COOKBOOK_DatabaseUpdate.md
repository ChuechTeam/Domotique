# Fiche pratique pour utiliser Liquibase

Liquibase nous permet de définir les différentes tables de la base de donnée, en écrivant du SQL.

Comme ça, on est tous sur la même version de la base de donnée.

## Mettre à jour la base de donnée

**Linux**
```bash
./liquibase
```
**Windows**
```bash
./liquibase.bat
```

## Annuler une modification

**Linux**
```bash
./liquibase rollbackCount 1
```

**Windows**
```bash
./liquibase.bat rollbackCount 1
```

Évidemment on peut changer `1` en n'importe quelle quantité.

## Modifier la base de donnée

Pour ça, il faut ajouter un "changeset" dans le fichier `src/main/resources/changelog.sql`.

Un changeset est composé de :
- des requêtes SQL de modification
- des requêtes SQL d'annulation (optionnel) précédes de `-- rollback`

**Exemple**
```sql
-- changeset mon_nom:id_unique
CREATE TABLE ma_table (
    id INT PRIMARY KEY,
    nom VARCHAR(255)
);
CREATE TABLE mon_autre_table (
    id INT PRIMARY KEY,
    nom VARCHAR(255)
);

-- rollback DROP TABLE ma_table;

-- Pour avoir une requête qui est sur plusieurs lignes :
-- rollback DROP 
-- rollback TABLE
-- rollback mon_autre_table;
```

