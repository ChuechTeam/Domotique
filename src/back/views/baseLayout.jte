@import gg.jte.Content

@param Content content = @``
@param Content head = @``
@param String title = "Domotique"

<html lang="fr">
<head>
    <title>${title}</title>
    <link rel="stylesheet" href="/style.css">
    ${head}
</head>
<body>
<h1>Mon site</h1>
<main>${content}</main>
</body>
</html>