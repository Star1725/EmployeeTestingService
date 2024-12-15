<#import "parts/common.ftl" as c>
<#include "parts/security.ftl">

<@c.page>
    <h3 class="mb-4">Это главная страница</h3>
    <div>
        Здесь будет отображаться информация по выбору тематики тестирования и запуске теста
    </div>
    logfile текущего пользователя:
    <div>
        <#if user.logFile??>
            <ul>
                <#list mapLog as key, value>
                    <li>${key}: ${value}</li>
                </#list>
            </ul>
        </#if>
    </div>

</@c.page>