<!-- security.ftl - вспомогательный файл, в котором описаны переменные для взаимодействия с SPRING_SECURITY_CONTEXT-->

<!-- проверяем, поместил ли Spring Security в контексте freemarker-а специальный объект, который позволяет оперировать контекстом Spring Security-->
<#assign
    know = SPRING_SECURITY_CONTEXT??
>

<#if know>
    <!-- если этот объект определён, значит можно работать с сессией пользователя -->
    <#assign
        user_SPRING_SECURITY_CONTEXT = SPRING_SECURITY_CONTEXT.authentication.principal
        name = user_SPRING_SECURITY_CONTEXT.getUsername()
        idUserContext = user_SPRING_SECURITY_CONTEXT.getId()
        isAdmin = user_SPRING_SECURITY_CONTEXT.isAdmin()
        isMainAdmin = user_SPRING_SECURITY_CONTEXT.isMainAdmin()
        rolesUserContext = user_SPRING_SECURITY_CONTEXT.roles
        levelsAccessUserContext = user_SPRING_SECURITY_CONTEXT.accessLevels
        specAccessUserContext = user_SPRING_SECURITY_CONTEXT.specAccesses
    >
<#else>
    <#assign
        name = "unknow"
        idUserContext = 999999
        isAdmin = false
        isMainAdmin = false
    >
</#if>