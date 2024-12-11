<!--Этот макрос называется 'page'. Он является базой веб-страницы. Вместо секции 'nested', будут вставляться основные templates-->
<#macro page>
    <!doctype html>
    <html lang="ru">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>My service for testing workers</title>

        <!-- подключение через сервер cdn
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
              crossorigin="anonymous">
        -->

        <!-- подключение через pom.xml (/webjars)-->
        <link rel="stylesheet" href="/webjars/bootstrap/5.3.3/css/bootstrap.min.css">

        <!-- локальное подключение через внутренний раздел static
        <link href="/static/css/bootstrap.min.css" rel="stylesheet" type="text/css">
        -->
    </head>
    <body>
    <#include "navbar.ftl">
    <div class="container-fluid mt-5 px-5">
        <#nested>
    </div>

    <!-- подключение через сервер cdn
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous"></script>
    -->
    <!-- локальное подключение через внутренний раздел static
    <script src="/static/js/bootstrap.min.js"></script>
    -->
    <!-- подключение через pom.xml (/webjars)-->
    <script src="/webjars/bootstrap/5.3.3/js/bootstrap.min.js"></script>

    </body>
    </html>
</#macro>