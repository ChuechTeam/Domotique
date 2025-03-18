<%-- Boring imports --%>
@import fr.domotique.data.*
@import java.util.*

@param Integer test

<%-- Use the root layout --%>
@template.baseLayout(
content = @`
    <h1>Bienvenue sur un site</h1>
    <p>Test + 10 = ${test+10}</p>
`,
title = "Accueil"
)