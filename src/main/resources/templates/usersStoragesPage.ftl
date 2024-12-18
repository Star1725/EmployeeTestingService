<#import "parts/common.ftl" as c>

<@c.page>

    <style>
        .tableUsersStorage {
            border-collapse: collapse;
            counter-reset: schetchik;
        }

        .tableUsersStorage tbody tr:nth-child(n+1) {
            counter-increment: schetchik;
        }

        .tableUsersStorage td,
        .tableUsersStorage tbody tr:before {
            padding: .1em .1em;
            border: 1px solid #D3D3D3;
        }

        .tableUsersStorage tbody tr:before {
            display: table-cell;
            vertical-align: center;
        }

        .tableUsersStorage tbody tr:before,
        .tableUsersStorage b:after {
            content: counter(schetchik);
            color: #1a1a1a;
        }

        .tableUsersStorage tbody tr:nth-child(-n+0):before {
            content: "";
        }
    </style>

    <div>
        <table class="tableUsersStorage table table-hover">
            <thead>
            <tr>
                <th></th>
                <th>Организация/Подразделение</th>
                <th>Администратор</th>
                <th>Кол-во сотрудников</th>
                <th>Средний балл</th>
                <th>Редактировать</th>
                <th>Удалить</th>
            </tr>
            </thead>
            <tbody>
            <#if usersStorages??>
                <#list usersStorages! as storage>
                    <tr>
                        <#--                        Подразделение-->
                        <td>${storage.usersStorageName}</td>
                        <#--                        Администратор-->
                        <td>
                            <#if
                            storage.administrator??>${storage.administrator.username}
                            </#if>
                        </td>
                        <#--                        Количество сотрудников-->
                        <td>${storage.storageUsers?size}</td>
                        <#--                        Средний балл-->
                        <td>75%</td>
                        <#--                        Редактировать-->
                        <td>
                            <#if storage.id == 0>
                                <button type="button" class="btn btn-success disabled" data-bs-toggle="modal"
                                        data-bs-target="#updateModal${storage.id}">
                                    Редактировать
                                </button>
                            <#else>
                                <button type="button" class="btn btn-success" data-bs-toggle="modal"
                                        data-bs-target="#updateModal${storage.id}">
                                    Редактировать
                                </button>
                            </#if>
                        </td>
                        <#--                        Удалить-->
                        <td>
                            <#if storage.id == 0>
                                <button type="button" class="btn btn-danger disabled" data-bs-toggle="modal"
                                        data-bs-target="#deleteModal${storage.id}">
                                    Удалить
                                </button>
                            <#else>
                                <button type="button" class="btn btn-danger" data-bs-toggle="modal"
                                        data-bs-target="#deleteModal${storage.id}">
                                    Удалить
                                </button>
                            </#if>
                        </td>
                    </tr>

                    <!-- модальное окно удаления -->
                    <div class="modal fade text-center" id="deleteModal${storage.id}" data-bs-backdrop="static"
                         data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel"
                         aria-hidden="true">
                        <div class="modal-dialog modal-lg">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h3 class="modal-title">Подтверждение удаления</h3>
                                    <button type="button" class="btn-close" data-bs-dismiss="modal"
                                            aria-label="Закрыть"></button>
                                </div>

                                <div class="modal-body">
                                    <span id="confirmText">Вы хотите удалить подразделение "${storage.usersStorageName}"?</span>
                                </div>

                                <div class="modal-footer">
                                    <a href="/usersStorage/delete/${storage.id}" type="button" id="yesBtn"
                                       class="btn btn-danger">Да</a>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!------------------------------>

                    <!-- модальное окно редактирования -->
                    <div class="modal fade" id="updateModal${storage.id}" data-bs-backdrop="static"
                         data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel"
                         aria-hidden="true">
                        <div class="modal-dialog modal-lg">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h3 class="modal-title">Обновление данных подразделения -
                                        "${storage.usersStorageName}
                                        "</h3>
                                    <button type="button" class="btn-close" data-bs-dismiss="modal"
                                            aria-label="Закрыть"></button>
                                </div>
                                <div class="modal-body">
                                    <form method="post" class="mt-2" action="/usersStorage/update/${storage.id}">
                                        <div class="mb-3">
                                            <label for="usersStorageName" class="col-form-label">Организация/подразделение</label>
                                            <input class="form-control ${(usersStorageNameError??)?string('is-invalid', '')}"
                                                   type="text"
                                                   name="usersStorageName"
                                                   id="usersStorageName"
                                                   placeholder="Название организации/подразделения"
                                                   value="<#if storage??>${storage.usersStorageName}</#if>"
                                            />
                                            <#if usersStorageNameError??>
                                                <div class="invalid-feedback">
                                                    ${usersStorageNameError}
                                                </div>
                                            </#if>
                                        </div>
                                        <div class="mb-3">
                                            <label for="storageDescription" class="col-form-label">Описание</label>
                                            <input class="form-control"
                                                   type="text"
                                                   name="storageDescription"
                                                   id="storageDescription"
                                                   placeholder="описание(при необходимости)"
                                                   value="<#if storage.storageDescription??>${storage.storageDescription}</#if>"
                                            />
                                        </div>
                                        <input type="hidden" name="_csrf" value="${_csrf.token}"/>
                                        <button class="btn btn-primary col-sm-2" type="submit">
                                            Обновить
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!------------------------------>

                </#list>
            </#if>
            </tbody>
        </table>
    </div>

    <div>
        <form method="post" class="mt-2">
            <div class="form-group row my-3">
                <a class="btn btn-info col-sm-3" data-bs-toggle="collapse" href="#collapseAddUsersStorage" role="button"
                   aria-expanded="false"
                   aria-controls="collapseExample">
                    Добавить организацию/подразделение
                </a>
            </div>
            <div class="collapse my-2 <#if usersStorage??>show</#if>" id="collapseAddUsersStorage">
                <div class="row my-2">
                    <div class="col-sm-7">
                        <input class="form-control ${(usersStorageNameError??)?string('is-invalid', '')}"
                               type="text"
                               name="usersStorageName"
                               placeholder="Название организации/подразделения"/>
                        <#if usersStorageNameError??>
                            <div class="invalid-feedback">
                                ${usersStorageNameError}
                            </div>
                        </#if>
                    </div>
                </div>
                <div class="row my-2">
                    <div class="col-sm-7">
                        <input class="form-control"
                               type="text"
                               name="storageDescription"
                               placeholder="описание(при необходимости)"/>
                    </div>
                </div>
                <div class="row my-2">
                    <div class="col-sm-7">
                        <input type="hidden" name="_csrf" value="${_csrf.token}"/>
                        <button class="btn btn-primary col-sm-2" type="submit">
                            Добавить
                        </button>
                    </div>
                </div>

                <span class="text-success">
                    <#if messageResultAdd??>
                        ${messageResultAdd}
                    </#if>
            </span>
            </div>
        </form>
    </div>

<#--скрипт открывающий модальное окно редактирования, если не прошли валидацию при обновлении данных для строки с id = openModalId-->
    <#if openModalId??>
        <script>
            document.addEventListener('DOMContentLoaded', function () {
                let modalId = '${openModalId!}';
                if (modalId) {
                    let modal = new bootstrap.Modal(document.getElementById(`updateModal${modalId}`));
                    modal.show();
                }
            });
        </script>
    </#if>
</@c.page>