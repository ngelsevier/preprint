<#macro layout title displaySearchBox styleExtensions="" scriptExtensions="">
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title} :: SSRN</title>
    <link type="text/css" rel="stylesheet" href="css/styles.css?v=5"/>
    ${styleExtensions}
    <script type="text/javascript" src="js/menu.js" async="async"></script>
    <script type="text/javascript" src="js/focusing.js" async="async"></script>
    <script type="text/javascript" src="js/authenticated.js" async="async"></script>
    ${scriptExtensions}
</head>
<body>
    <#include "header.ftl"/>

    <#if displaySearchBox>
        <#include "/com/ssrn/frontend/website/search/searchBox.ftl"/>
    </#if>

<main>
    <#nested />
</main>

    <#include "footer.ftl"/>
</body>
</html>
</#macro>