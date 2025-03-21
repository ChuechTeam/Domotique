package fr.domotique.data;

import fr.domotique.apidocs.*;

@DocDesc("The level of the user: how advanced they are and what are its permissions in the EHPAD.")
public enum Level {
    /// A fresh new user of the app
    ///
    /// **ACCESS TO**: Nothing
    ///
    /// > Débutant : Vous avez le niveau d'expérience acquis en classe et/ou dans des scénarios expérimentaux ou en
    /// tant que stagiaire sur le lieu de travail. On  s'attend à ce que vous ayez besoin d'aide dans l'exercice de cette compétence.
    BEGINNER,

    /// A user who has some experience with the app
    ///
    /// **ACCESS TO**: Nothing
    ///
    /// > Intermédiaire : Vous pouvez accomplir avec succès les tâches qui vous sont  demandées dans le cadre de cette compétence.
    /// L'aide d'un expert peut s'avérer  nécessaire de temps à autre, mais vous pouvez généralement exécuter cette
    /// compétence de manière autonome.
    INTERMEDIATE,

    /// A user who is very experienced with the app
    ///
    /// **ACCESS TO**: "Managment" module
    ///
    /// > Avancé : Vous pouvez effectuer les actions associées à cette compétence sans assistance. Vous êtes certainement reconnu au sein de votre organisation
    /// immédiate comme "la personne à qui demander" lorsque des questions difficiles se posent au sujet de cette compétence.
    ADVANCED,

    /// The ultimatest user of the app.
    ///
    /// **ACCESS TO**: "Admin" module
    ///
    /// > Expert : Vous êtes reconnu comme un expert dans ce domaine.
    /// Vous pouvez fournir des conseils, résoudre des problèmes et répondre à des questions liées
    ///  à ce domaine d'expertise et au domaine dans lequel la compétence est utilisée.
    EXPERT;

    /// Converts a byte value to a Level enum.
    public static Level fromByte(byte b) {
        return values()[b];
    }
}
