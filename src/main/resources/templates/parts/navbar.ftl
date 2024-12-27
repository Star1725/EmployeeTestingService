<#include "security.ftl">
<#import "login.ftl" as l>

<nav class="navbar navbar-expand-lg bg-body-tertiary">
    <div class="container-fluid">
        <a class="navbar-brand" href="/">Тестировщик 1.0</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent"
                aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Переключатель навигации">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link" aria-current="page" href="/mainPage">Главная</a>
                </li>
                <#--                <#if userDTO??>-->
                <#--                    <li class="nav-item">-->
                <#--                        <a class="nav-link" aria-current="page" href="/subjectQuestion">Тематика</a>-->
                <#--                    </li>-->
                <#--                </#if>-->
                <#--                <#if userDTO??>-->
                <#--                    <li class="nav-item">-->
                <#--                        <a class="nav-link" aria-current="page" href="/mainPage">Вопросы</a>-->
                <#--                    </li>-->
                <#--                </#if>-->
                <#if isMainAdmin>
                    <li class="nav-item">
                        <a class="nav-link" href="/userStorage">Организация/подразделение</a>
                    </li>
                </#if>
                <#if isAdmin>
                    <li class="nav-item">
                        <a class="nav-link" href="/users">Список пользователей</a>
                    </li>
                </#if>
                <#if user_SPRING_SECURITY_CONTEXT??>
                    <li class="nav-item">
                        <a class="nav-link" href="/users/profile/${idUserContext}">Профиль</a>
                    </li>
                </#if>
            </ul>
            <div class="navbar-text mx-3">${name}</div>
            <#if user_SPRING_SECURITY_CONTEXT??>
                <form action="/logout" method="post">
                    <input type="hidden" name="_csrf" value="${_csrf.token}"/>
                    <button class="btn btn-primary" type="submit">Выход</button>
                </form>
            <#else>
                <form action="/login" method="post">
                    <input type="hidden" name="_csrf" value="${_csrf.token}"/>
                    <button class="btn btn-primary" type="submit">Войти</button>
                </form>
            </#if>
        </div>
    </div>
</nav>