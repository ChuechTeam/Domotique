package fr.domotique.data;
import io.vertx.core.AbstractVerticle;  // Permet de créer un composant exécutable
import io.vertx.core.Vertx;             // La classe principale pour démarrer Vert.x
import io.vertx.ext.web.Router;         // Permet de gérer les URL de notre serveur
import io.vertx.ext.web.handler.BodyHandler;  // Permet de lire le contenu envoyés par le client
import io.vertx.ext.web.FileUpload;     // Permet de gérer les fichiers envoyés par l'utilisateur

// On crée une classe qui définit notre serveur
public class MonServeurDePhoto extends AbstractVerticle {

    // La méthode start() est lancée quand le serveur démarre
    @Override
    public void start() {
        // On crée un "router" qui va décider que faire quand une URL est appelée
        Router router = Router.router(vertx);

        // On ajoute un gestionnaire pour lire le contenu des requêtes HTTP
        // Ici, on précise que les fichiers envoyés seront temporairement stockés dans le dossier "uploads"
        router.route().handler(BodyHandler.create().setUploadsDirectory("uploads"));

        // On définit une route pour l'envoi de fichiers sur l'URL "/upload"
        router.post("/upload").handler(ctx -> {
            // On récupère le fichier envoyé par l'utilisateur
            FileUpload file = ctx.fileUploads().getFirst();

            // On récupère le chemin temporaire du fichier
            String tempPath = file.uploadedFileName();

            // On définit où le fichier sera définitivement stocké
            // Ici, on le place dans le dossier "uploads" en gardant son nom original.
            String finalPath = "uploads/" + file.fileName();

            // On déplace le fichier du dossier temporaire vers le dossier final
            // ça compile pas
//            vertx.fileSystem().move(tempPath, finalPath, result -> {
//                // Si le déplacement fonctionne, on renvoie un message indiquant que tout s'est bien passé
//                if (result.succeeded()) {
//                    ctx.response().end("Photo uploadée: " + file.fileName());
//                } else {
//                    // Sinon, on renvoie une erreur
//                    ctx.response().setStatusCode(500).end("Erreur lors de l'upload");
//                }
//            });
        });

        // On crée et démarre le serveur HTTP en utilisant le router pour traiter les requêtes
        // Le serveur écoute sur le port 8888.
        vertx.createHttpServer().requestHandler(router).listen(8888);
    }

    // La méthode main() est le point de départ de l'application
    public static void main(String[] args) {
        // On crée une instance de Vert.x, qui gère tout le serveur
        Vertx vertx = Vertx.vertx();

        // On déploie notre serveur pour qu'il commence à fonctionner
        vertx.deployVerticle(new MonServeurDePhoto());
    }
}
