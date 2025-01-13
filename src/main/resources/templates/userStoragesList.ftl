<#import "parts/common.ftl" as c>

<@c.page>

    <style>
        .tableUserStorage {
            border-collapse: collapse;
            counter-reset: schetchik;
        }

        .tableUserStorage tbody tr:nth-child(n+1) {
            counter-increment: schetchik;
        }

        .tableUserStorage td,
        .tableUserStorage tbody tr:before {
            padding: .1em .1em;
            border: 1px solid #D3D3D3;
        }

        .tableUserStorage tbody tr:before {
            display: table-cell;
            vertical-align: center;
        }

        .tableUserStorage tbody tr:before,
        .tableUserStorage b:after {
            content: counter(schetchik);
            color: #1a1a1a;
        }

        .tableUserStorage tbody tr:nth-child(-n+0):before {
            content: "";
        }
    </style>
    <h3>
        <#if allParentStoragesNames??>
            ${allParentStoragesNames}
        </#if>
    </h3>
    <div style="margin: 0 auto; text-align: left;">
        <table class="tableUserStorage table table-hover"
               style="margin: 0 auto; border-collapse: collapse; width: 100%;">
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
            <#if userStorages??>
                <#list userStorages! as storage>
                    <tr>
                        <#--Подразделение-->
                        <td>
                            <#if storage.childUserStorages?? && storage.childUserStorages?size != 0>
                                <a href="/userStorage/${storage.id}">${storage.userStorageName}</a>
                            <#else>
                                ${storage.userStorageName}
                            </#if>
                        </td>
                        <#--Администратор-->
                        <td>
                            <#if
                            storage.administrator??>${storage.administrator.username}
                            </#if>
                        </td>
                        <#--Количество сотрудников-->
                        <td>
                            <#if storage??>
                                <a href="/users/storage/${storage.id}">${storage.getAllNestedStorageUsers(storage)?size}</a>
                            </#if>
                        </td>
                        <#--Средний балл-->
                        <td>75%</td>
                        <#--Редактировать-->
                        <td>
                            <button type="button" class="btn btn-success" data-bs-toggle="modal"
                                    data-bs-target="#updateModal${storage.id}">
                                Редактировать
                            </button>
                        </td>
                        <#--Удалить-->
                        <td>
                            <button type="button" class="btn btn-danger" data-bs-toggle="modal"
                                    data-bs-target="#deleteModal${storage.id}">
                                Удалить
                            </button>
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
                                    <span id="confirmText">Вы хотите удалить подразделение "${storage.userStorageName}"?</span>
                                </div>

                                <div class="modal-footer">
                                    <a href="/userStorage/delete/${storage.id}" type="button" id="yesBtn"
                                       class="btn btn-danger">Да</a>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Модальное окно редактирования -->
                    <div class="modal fade" id="updateModal${storage.id}" data-bs-backdrop="static"
                         data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel"
                         aria-hidden="true">
                        <div class="modal-dialog modal-lg">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h3 class="modal-title">Обновление данных подразделения -
                                        "${storage.userStorageName}"
                                    </h3>
                                    <button type="button" class="btn-close" data-bs-dismiss="modal"
                                            aria-label="Закрыть"></button>
                                </div>
                                <div class="modal-body px-2">
                                    <form method="post" class="mt-2" action="/userStorage/update/${storage.id}">
                                        <div class="form-group row my-2 px-3">
                                            <label for="userStorageName" class="col-form-label">
                                                Название организации/подразделения
                                            </label>
                                            <input class="form-control ${(userStorageNameUpdateError??)?string('is-invalid', '')}"
                                                   type="text"
                                                   name="userStorageName"
                                                   id="userStorageName"
                                                   placeholder="Введите новое название"
                                                   value="<#if updatedUserStorage??>${updatedUserStorage.userStorageName}</#if>"
<#--                                                   value="<#if storage??>${storage.userStorageName}</#if>"-->
                                                   style="width: 100%;"/>
                                            <#if userStorageNameUpdateError??>
                                                <div class="invalid-feedback">
                                                    ${userStorageNameUpdateError}
                                                </div>
                                            </#if>
                                        </div>
                                        <div class="form-group row my-2 px-3">
                                            <label for="usersStorageName" class="col-form-label">
                                                Название вышестоящей организации/подразделение
                                            </label>
                                            <select class="form-select ${(userStorageParentNameSelectedError??)?string('is-invalid', '')}"
                                                    name="userStorageParentNameSelected"
                                                    aria-label="Выберите организацию"
                                                    style="width: 100%;">
                                                <option selected>
                                                    <#if storage.parentUserStorage??>
                                                        ${storage.getParentUserStorage().userStorageName}
                                                    </#if>
                                                </option>
                                                <#list userStorages! as userStorage>
                                                    <#if storage.getId() != userStorage.getId()>
                                                        <option value="${userStorage.userStorageName}"
                                                                name="userStorageParentName_Selected">${userStorage.userStorageName}</option>
                                                    </#if>
                                                </#list>
                                            </select>
                                            <#if userStorageParentName_SelectedError??>
                                                <div class="invalid-feedback">
                                                    ${userStorageParentNameSelectedError}
                                                </div>
                                            </#if>
                                        </div>
                                        <div class="form-group row my-2 px-3">
                                            <label for="storageDescription" class="col-form-label">Описание</label>
                                            <input class="form-control"
                                                   type="text"
                                                   name="storageDescription"
                                                   id="storageDescription"
                                                   placeholder="описание(при необходимости)"
                                                   value="<#if storage.storageDescription??>${storage.storageDescription}</#if>"
                                                   style="width: 100%;"/>
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
    <div style="margin: 0 auto; text-align: left;">
        <form class="mt-2" method="post" action="/userStorage/add">
            <div class="form-group row my-3" style="margin: 0 auto; text-align: left;">
                <a aria-controls="collapseExample" aria-expanded="false" class="btn btn-info col-sm-3"
                   data-bs-toggle="collapse" href="#collapseAddUserStorage" role="button">
                    Добавить организацию/подразделение
                </a>
            </div>
            <div class="collapse my-2 <#if userStorage??>show</#if>" id="collapseAddUserStorage"
                 style="margin: 0 auto; text-align: left;">
                <div class="form-group row my-2" style="margin: 0 auto; text-align: left;">
                    <label class="col-sm-3 col-form-label">Название организации/подразделения</label>
                    <div class="col-sm-5" style="text-align: left;">
                        <input class="form-control ${(userStorageNameError??)?string('is-invalid', '')}"
                               name="userStorageName" placeholder="Введите название организации/подразделения"
                               type="text"
                               value="<#if userStorage??>${userStorage.userStorageName}</#if>"/>
                        <#if userStorageNameError??>
                            <div class="invalid-feedback" style="margin: 0 auto; text-align: left;">
                                ${userStorageNameError}
                            </div>
                        </#if>
                    </div>
                </div>
                <div class="form-group row my-2" style="margin: 0 auto; text-align: left;">
                    <label class="col-sm-3 col-form-label">Вышестоящая организация/подразделение:</label>
                    <div class="col-sm-5" style="text-align: center;">
                        <select aria-label=".form-select-sm-2" class="form-select form-select-sm-3"
                                name="userStorageParentNameSelected">
                            <option selected="">
                                <#if parentUserStorage??>
                                    ${parentUserStorage.userStorageName}
                                </#if>
                            </option>
                            <#list userStorages! as userStorage>
                                <option name="userStorageParentNameSelected"
                                        value="${userStorage.userStorageName}">${userStorage.userStorageName}</option>
                            </#list>
                        </select>
                    </div>
                </div>
                <div class="form group row my-2" style="margin: 0 auto; text-align: left;">
                    <label class="col-sm-3 col-form-label">Описание:</label>
                    <div class="col-sm-7" style="text-align: left;">
                        <input class="form-control" name="storageDescription"
                               placeholder="введите описание (при необходимости)" type="text"/>
                    </div>
                </div>
                <div class="form group row my-2" style="margin: 0 auto; text-align: left;">
                    <div class="col-sm-7" style="text-align: left;">
                        <input name="_csrf" type="hidden" value="${_csrf.token}"/>
                        <input name="idParentStorage" type="hidden" value="<#if parentUserStorage??>
                                    ${parentUserStorage.id}
                                </#if>"/>
                        <button class="btn btn-primary col-sm-4" type="submit">
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
                let modal = new bootstrap.Modal(document.getElementById(`updateModal${openModalId}`));
                modal.show();
            });
        </script>
    </#if>
</@c.page>