# Guide de Futures dans Vert.x

## Création de Futures

```java
// Créer un future réussi avec une valeur
Future<String> successFuture = Future.succeededFuture("success");

// Créer un future échoué avec une exception
Future<String> failedFuture = Future.failedFuture(new RuntimeException("failed"));

// Créer un future à compléter plus tard
Promise<String> promise = Promise.promise();
Future<String> future = promise.future();

// Compléter la promise (et son future) plus tard
promise.complete("result");
// Ou la faire échouer
promise.fail("something went wrong");
```

## Gérer les résultats des Futures

**Attention** : Il est interdit d'enchaîner plusieurs `onSuccess` de suite (idem pour `onFailure` et `onComplete`) !

```java
future
    .onSuccess(result -> {
        System.out.println("Succès: " + result);
    })
    .onFailure(error -> {
        System.err.println("Erreur: " + error.getMessage());
    })
    .onComplete(ar -> {
        if (ar.succeeded()) {
            System.out.println("Complété avec: " + ar.result());
        } else {
            System.err.println("Échec avec: " + ar.cause().getMessage());
        }
    });
```

## Transformer des Futures

```java
// Transformer le résultat avec map
Future<Integer> lengthFuture = future.map(str -> str.length());

// Transformer le résultat de façon asynchrone avec flatMap (retourne un nouveau Future)
Future<User> userFuture = idFuture.flatMap(id ->
    databaseClient.query("SELECT * FROM users WHERE id = ?")
        .execute(Tuple.of(id))
        .map(rows -> new User(rows.iterator().next()))
);

// Exécuter un effet secondaire sans changer le résultat
Future<String> loggedFuture = future.andThen(ar -> {
    if (ar.succeeded()) {
        log.info("Opération réussie avec: {}", ar.result());
    } else {
        log.error("Opération échouée", ar.cause());
    }
});
```

## Composer des Futures

```java
// Exécuter deux futures en séquence
Future<String> composedFuture = future1.compose(result -> {
    return future2; // future2 est exécuté après que future1 soit complété
});

// Transformer avec compose - comme flatMap
Future<DBUser> userFuture = getUserIdFuture().compose(id ->
    fetchUserFromDatabase(id)
);

// Enchaîner plusieurs opérations
Future<String> result = getUser()
    .compose(user -> updateUser(user))
    .compose(updatedUser -> notifyUser(updatedUser))
    .compose(notification -> logAction(notification));
```

## Combiner plusieurs Futures

```java
// Attendre que tous les futures soient complétés
CompositeFuture.all(future1, future2, future3)
    .onSuccess(cf -> {
        // Tous ont réussi
        String result1 = cf.resultAt(0);
        Integer result2 = cf.resultAt(1);
        Boolean result3 = cf.resultAt(2);
    });

// Réussir quand n'importe quel future réussit
CompositeFuture.any(future1, future2)
    .onSuccess(cf -> {
        // Au moins un a réussi
    });

// Réussir quand tous réussissent, échouer quand n'importe lequel échoue
List<Future> futures = new ArrayList<>();
futures.add(future1);
futures.add(future2);
CompositeFuture.join(futures)
    .onComplete(ar -> {
        // Appelé avec le résultat de tous les Futures
    });
```

## Attente de Futures (Threads Virtuels)

```java
// Utiliser .await() avec des threads virtuels
// Fonctionne seulement avec le déploiement en threads virtuels (ThreadingModel.VIRTUAL_THREAD)
try {
    String result = future.await();
    System.out.println("Résultat obtenu: " + result);
} catch (Exception e) {
    System.err.println("Future échoué: " + e.getMessage());
}

// Utiliser await() avec plusieurs futures
try {
    String user = getUserFuture().await();
    List<Order> orders = getOrdersFuture(user).await();
    Double total = calculateTotalFuture(orders).await();
    System.out.println("Total: " + total);
} catch (Exception e) {
    System.err.println("Processus échoué: " + e.getMessage());
}
```

## Gestion des erreurs

```java
// Récupérer après une erreur
Future<String> recoveredFuture = future.recover(error -> {
    // Retourner un nouveau résultat si le future original échoue
    return Future.succeededFuture("valeur de secours");
});

// Mapper les erreurs
Future<String> mappedErrorFuture = future.otherwise(error -> {
    // Retourner une valeur si le future échoue
    return "valeur par défaut";
});

// Mapper des erreurs spécifiques
Future<String> specificErrorFuture = future.otherwise(IllegalArgumentException.class, e -> {
    return "argument illégal géré";
});
```

## Utilitaires

```java
// Vérifier si un future est terminé
boolean isDone = future.isComplete();

// Vérifier si un future a réussi
if (future.succeeded()) {
    String result = future.result();
}

// Vérifier si un future a échoué
if (future.failed()) {
    Throwable cause = future.cause();
}

// Obtenir le résultat (peut lever une exception si non réussi)
String result = future.result();

// Obtenir la cause de l'échec (peut être null si réussi)
Throwable cause = future.cause();

// Convertir un Future en Promise
Promise<String> promise = Promise.promise();
Future<String> future = promise.future();
```

## Conseils et bonnes pratiques

1. Toujours gérer les échecs avec `onFailure()` ou `onComplete()`
2. Utiliser `compose()` pour des opérations asynchrones séquentielles
3. Éviter les opérations bloquantes dans les callbacks
4. Utiliser `CompositeFuture` pour des opérations parallèles
5. Dans les threads virtuels, utiliser `await()` pour un code plus lisible
6. Retourner `Future<?>` depuis les méthodes pour permettre le chaînage