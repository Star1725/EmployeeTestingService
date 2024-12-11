<#import "parts/common.ftl" as c>
<#import "parts/login.ftl" as l>
<@c.page>
    <h3 class="mb-4">Добавить нового пользователя</h3>
    <#if message??>
        <div class="alert alert-warning" role="alert">${message!}</div>
    </#if>
    <@l.login "/registration" true/>
</@c.page>