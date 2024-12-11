<#import "parts/common.ftl" as c>
<#import "parts/login.ftl" as l>
<#include "parts/security.ftl">

<@c.page>
    <h3 class="mb-4">Авторизация</h3>
    <@l.login "/login" false/>
</@c.page>