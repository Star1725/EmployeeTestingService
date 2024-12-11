<#import "parts/common.ftl" as c>
<!--Чтобы вывести активна ли данная роль пользователя - ?seq_contains(role) -
    этот метод проверяет наличие элемента в коллекции и возвращает булево значение.
    ?string("checked", "") - преобразовывает булево значение в строку-->
<@c.page>
    <h3>User editor</h3>
    <form action="/user" method="post">
        <input type="text" name="username" value="${user.username}">
        <#list roles as role>
            <div>
                <label><input type="checkbox" name="${role}" ${user.roles?seq_contains(role)?string("checked", "")}>${role}</label>
            </div>
        </#list>
        <input type="hidden" name="userId" value="${user.id}">
        <input type="hidden" name="_csrf" value="${_csrf.token}"/>
        <button type="submit">Save</button>
    </form>
</@c.page>