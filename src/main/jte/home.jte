<%-- Boring imports --%>
@import fr.domotique.data.*
@import java.util.*

<%-- Comes from the MainController: model.addAttribute("users", users);  --%>
@param List<User> users

<%-- Use the root layout --%>
@template.layout.root(
content = @`
    <p>Bienvenue sur un site</p>
    <h2>Utilisateurs inscrits</h2>
    <ul>
        @for (User u : users)
            <li>Id = ${u.getId()} ; Email = ${u.getEmail()} ; Genre = ${u.getGender().name()}</li>
        @endfor
    </ul>
`,
title = "Accueil"
)