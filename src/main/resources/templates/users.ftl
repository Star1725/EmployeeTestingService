<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
    <form action="/logout" method="post">
        <input type="submit" value="Sign out"/>
        <input type="hidden" name="_csrf" value="${_csrf.token}"/>
    </form>
    <div>
        Hello ${userName}
    </div>
</body>
</html>