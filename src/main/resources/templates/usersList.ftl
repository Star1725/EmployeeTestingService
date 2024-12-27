<#import "parts/common.ftl" as c>
<#include "parts/security.ftl">

<@c.page>
    <h3 class="mb-4">List of users</h3>

    <div>
        текущий администратор - ${name}, его "id" = ${idUserContext}
    </div>

    <style>
        .tableUsers {
            border-collapse: collapse;
            counter-reset: schetchik;
        }
        .tableUsers tbody tr:nth-child(n+1) {
            counter-increment: schetchik;
        }
        .tableUsers td,
        .tableUsers tbody tr:before {
            padding: .1em .1em;
            border: 1px solid #D3D3D3;
        }
        .tableUsers tbody tr:before {
            display: table-cell;
            vertical-align: center;
        }
        .tableUsers tbody tr:before,
        .tableUsers b:after {
            content: counter(schetchik);
            color: #1a1a1a;
        }
        .tableUsers tbody tr:nth-child(-n+0):before {
            content: "";
        }
        .custom-paragraph {
            color: black;
            /*font-size: 18px;*/
            margin: 0;
            padding: 0;
            /*border: 1px solid gray;*/
        }
    </style>

    <table class="tableUsers table table-hover">
        <thead>
        <tr>
            <th scope="col"></th>
            <th scope="col">ФИО</th>
            <th scope="col">Подразделение</th>
            <th scope="col">Роль</th>
            <th scope="col">Уровень доступа</th>
            <th scope="col">Спец. допуски</th>
            <th scope="col">Редактировать</th>
            <th scope="col">Удалить</th>
        </tr>
        </thead>
        <tbody>
        <#list users as user>
            <tr>
                <!--Имя пользователя-->
                <td>${user.username}</td>
                <!--Подразделение-->
                <td>
                    <#if user.userStorage??>
                        ${user.userStorage.getUserStorageName()}
                    </#if>
                </td>
                <!--Роли будем выводить в виде списка в строчку через запятую (#sep)-->
                <td>
                    <#list user.roles as role>
                    <p class="custom-paragraph">${role}
                    </#list>
                </td>
                <!--Уровни доступа будем выводить в виде списка в строчку через запятую (#sep) или с новой строки (p)-->
                <td>
                    <#list user.accessLevels as access>
                        <p class="custom-paragraph">${access}
                    </#list>
                </td>
                <!--Допуск к ???-->
                <td>
                    <#list user.specAccesses as specAccses>
                        <p class="custom-paragraph">${specAccses}
                    </#list>
                </td>
                <!--Редактировать-->
                <td>
                    <#if user.id == idUserContext || user.isMainAdmin()>
                        <a href="/users/profile/${user.id}" class="btn btn-success disabled" aria-disabled="true">Редактировать</a>
                        <#else >
                            <a href="/users/profile/${user.id}" class="btn btn-success">Редактировать</a>
                    </#if>
                </td>
                <!--Удалить-->
                <td>
                    <#if user.id == idUserContext || user.isMainAdmin()>
                        <button type="button" class="btn btn-danger disabled" aria-disabled="true" data-bs-toggle="modal"
                                data-bs-target="#deleteModal${user.id}">
                            Удалить
                        </button>
                    <#else >
                        <button type="button" class="btn btn-danger" data-bs-toggle="modal"
                                data-bs-target="#deleteModal${user.id}">
                            Удалить
                        </button>
                    </#if>
                </td>
            </tr>

            <!-- модальное окно удаления -->
            <div class="modal fade text-center" id="deleteModal${user.id}" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header">
<#--                            <h3 class="modal-title text-center">-->
                                Подтверждение удаления
<#--                            </h3>-->
                            <button type="button" class="btn-close" data-bs-dismiss="modal"
                                    aria-label="Закрыть"></button>
                        </div>
                        <div class="modal-body">
                            <span id="confirmText">Вы хотите удалить пользователя "${user.getUsername()}"?</span>
                        </div>
                        <div class="modal-footer">
                            <a href="/users/delete/${user.id}" type="button" id="yesBtn"
                               class="btn btn-danger">Да</a>
                        </div>
                    </div>
                </div>
            </div>
            <!------------------------------>
        </#list>
        </tbody>
    </table>

</@c.page>