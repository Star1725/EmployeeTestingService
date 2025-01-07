<#import "parts/common.ftl" as c>
<#include "parts/security.ftl">

<@c.page>

    <style>
        #loadingMessage {
            font-size: 14px;
            color: #555;
            margin-top: 10px;
            font-style: italic;
        }
    </style>


    <#if userDTO.id == idUserContext>
        <h3 class="mb-2">Ваш профиль</h3>
    <#else >
        <h3 class="mb-2">Профиль пользователя ${userDTO.username}</h3>
    </#if>
    <form method="post" class="mt-2">
        <!-- Имя пользователя ------------------------------------------------------------------------------------->
        <div class="form-group row my-3">
            <label class="col-sm-2 col-form-label">Имя пользователя:</label>
            <div class="col-sm-5">
                <input class="form-control ${(usernameNewError??)?string('is-invalid', '')}"
                       type="text" name="usernameNew"
                       value="<#if userDTO??>${userDTO.username}</#if>"
                       placeholder="ФИО"/>
                <#if usernameNewError??>
                    <div class="invalid-feedback">
                        ${usernameNewError}
                    </div>
                </#if>

            </div>
        </div>
        <!-- Организация ---------------------------------------------------------------------------------------------->
        <div class="form-group row my-2">
            <label class="col-sm-2 col-form-label">Организация/подразделение:</label>
            <div class="col-sm-5">
                <select class="form-select form-select-sm-2"
                        name="primaryParentStorageNameSelected"
                        aria-label=".form-select-sm-2"
                        id="primaryParentStorageNameSelected">
                    <option selected>
                        <#if userDTO.isMainAdmin()>
                            ${userStorageDTO.defaultPrimaryParentStorage.userStorageName}
                        <#else >
                            <#if userStorageDTO.primaryParentStorage??>
                                ${userStorageDTO.primaryParentStorage.userStorageName}
                            </#if>
                        </#if>
                    </option>
                    <#if !userDTO.isMainAdmin()>
                        <#list userStorageDTO.allPrimaryParentStorages! as primaryParentStorage>
                        <option value="${primaryParentStorage.getId()}"
                                name="primaryParentStorageNameSelected"
                        >${primaryParentStorage.userStorageName}
                        </option>
                    </#list>
                    </#if>
                </select>
            </div>
        </div>

        <!-- Администратор подразделения / подразделение -------------------------------------------------------------->
        <#if !userDTO.isMainAdmin()>
            <div class="form-group row my-2">
                <label class="col-sm-2 col-form-label">
                    <#if userDTO.isAdmin()>
                        Администратор организации/подразделения:
                    <#elseif userDTO.isUser()>
                        подразделение:
                    </#if>
                </label>

                <div class="col-sm-5">
                    <select class="form-select form-select-sm-2"
                            name="storageId_Selected"
                            aria-label=".form-select-sm-2"
                            id="storageName_Selected">
                        <option selected>
                            <#if userStorageDTO.fullUserStorageName??>
                                ${userStorageDTO.fullUserStorageName}
                            </#if>
                        </option>
                        <#list userStorageDTO.allChildStoragesForPrimaryParent! as childStorage>
                            <option value="${childStorage.id}"
                                    name="storageId_Selected"
                            >${childStorage.fullUserStorageName}</option>
                        </#list>
                    </select>
                </div>
                <div class="col-sm-3" id="loadingMessage" style="display: none;">Загрузка...</div>
            </div>
        </#if>

        <#--скрипт динамтчески подгружающий список дочерних подразделений в тег <select id="storageName_Selected"> в зависимости от организации, выбранной в <select id="parentStorageName_Selected">>-->
        <script>
            /*
                Находим элемент с id="parentStorageName_Selected".
                Добавляем обработчик события change, который срабатывает, когда пользователь выбирает другое значение в <select>.
             */
            document.getElementById('primaryParentStorageNameSelected').addEventListener('change', function () {
                /*
                    this указывает на элемент, вызвавший событие (в данном случае, <select>).
                    this.value получает значение выбранного пункта (value атрибута <option>).
                 */
                const selectedParentStorage = this.value;
                /*
                    Находим элемент с id="loadingMessage" (для показа сообщения "Загрузка").
                    Находим элемент с id="storageName_Selected" (второй <select>), чтобы динамически обновить его содержимое.
                 */
                const loadingMessage = document.getElementById('loadingMessage');
                const childSelect = document.getElementById('storageName_Selected');

                // Показать сообщение "Загрузка..."
                /*
                    Устанавливаем display: block для loadingMessage, чтобы оно стало видимым.
                    Блокируем второй <select> (disabled = true), чтобы пользователь не взаимодействовал с ним до завершения загрузки.
                 */
                loadingMessage.style.display = 'block';
                childSelect.disabled = true; // Заблокировать второй select во время загрузки
                /*
                    Отправляем запрос к серверу на URL /userStorage/{selectedParentStorage}/childStorages.
                    selectedParentStorage подставляется в запрос как идентификатор выбранной организации.
                 */
                <#--fetch(`/userStorage/${selectedParentStorage}/childStorages`)-->
                fetch(`/userStorage/` + selectedParentStorage + `/childStorages`)
                    /*
                        Когда сервер возвращает ответ, он преобразуется из JSON-формата в объект JavaScript, который удобен для обработки.
                     */
                    .then(response => response.json())
                    .then(data => {
                        // Очищаем содержимое второго <select>, чтобы удалить предыдущие опции.
                        childSelect.innerHTML = '';
                        /*
                            Перебираем массив data (список дочерних подразделений) из ответа сервера.
                            Для каждого элемента создаём новый тег <option>:
                            - option.value устанавливает значение для опции.
                            - option.textContent задаёт текст, который видит пользователь.
                            Добавляем созданный <option> в <select> с помощью appendChild.
                         */
                        data.forEach(child => {
                            const option = document.createElement('option');
                            option.value = child.id;
                            option.textContent = child.fullUserStorageName;
                            childSelect.appendChild(option);
                        });
                        /*
                            После успешной загрузки скрываем сообщение "Загрузка" (display: none).
                            Разблокируем второй <select> (disabled = false), чтобы пользователь мог выбрать из обновлённых опций.
                         */
                        loadingMessage.style.display = 'none';
                        childSelect.disabled = false; // Разблокировать select
                    })
                    /*
                        Если запрос завершился с ошибкой, выводим её в консоль с помощью console.error.
                        Скрываем сообщение "Загрузка" и разблокируем второй <select>, чтобы интерфейс оставался доступным.
                     */
                    .catch(error => {
                        console.error('Ошибка при загрузке данных:', error);

                        // Скрыть сообщение "Загрузка..." даже при ошибке
                        loadingMessage.style.display = 'none';
                        childSelect.disabled = false;
                    });
            });
        </script>

        <!-- Роль ------------------------------------------------------------------------------------------------->
        <#if isAdmin>
            <div class="form-group row my-3">
                <label class="col-sm-2 col-form-label">Роль:</label>
                <div class="col-sm-5">
                    <#list roles as role>
                        <div>
                            <label>
                                <input class="form-check-input" type="checkbox" name="${role}"
                                        <#if userDTO.id == idUserContext>
                                            ${userDTO.roles?seq_contains(role)?string("checked disabled", "disabled")}
                                        <#else>
                                            <#if rolesUserContext?seq_contains(role)>
                                                <#if role == "MAIN_ADMIN" || role == "ADMIN"> disabled
                                                <#else>
                                                    <#if userDTO.roles?seq_contains(role)>checked
                                                    <#else>
                                                    </#if>
                                                </#if>
                                            <#else>disabled
                                            </#if>
                                        </#if>
                                >
                                ${role}
                            </label>
                        </div>
                    </#list>
                </div>
            </div>
            <!-- Уровень доступа--------------------------------------------------------------------------------------->
            <div class="form-group row my-3">
                <label class="col-sm-2 col-form-label">Уровень доступа:</label>
                <div class="col-sm-5">
                    <#list accessLevels as level>
                        <div>
                            <label>
                                <input class="form-check-input" type="checkbox" name="${level}"
                                        <#if userDTO.id == idUserContext>
                                            ${userDTO.accessLevels?seq_contains(level)?string("checked disabled", "disabled")}
                                        <#else >
                                            <#if levelsAccessUserContext?seq_contains(level)>
                                                <#if userDTO.accessLevels?seq_contains(level)>checked
                                                <#else>
                                                </#if>
                                            <#else>disabled
                                            </#if>
                                        </#if>
                                >
                                ${level}
                            </label>
                        </div>
                    </#list>
                </div>
            </div>
            <!-- Спец. допуск------------------------------------------------------------------------------------------>
            <div class="form-group row my-3">
                <label class="col-sm-2 col-form-label">Специальные допуски:</label>
                <div class="col-sm-5">
                    <#list specAccesses as specAccess>
                        <div>
                            <label>
                                <input class="form-check-input" type="checkbox" name="${specAccess}"
                                        <#if userDTO.id == idUserContext>
                                            ${userDTO.specAccesses?seq_contains(specAccess)?string("checked disabled", "disabled")}
                                        <#else >
                                            <#if specAccessUserContext?seq_contains(specAccess)>
                                                <#if userDTO.specAccesses?seq_contains(specAccess)>checked
                                                <#else>
                                                </#if>
                                            <#else>disabled
                                            </#if>
                                        </#if>
                                >
                                ${specAccess}
                            </label>
                        </div>
                    </#list>
                </div>
            </div>
        </#if>
        <div class="form-group row my-3">
            <button class="btn btn-primary col-sm-2" type="submit">Сохранить</button>
        </div>
        <div class="form-group row my-3">
            <a class="btn btn-info col-sm-2" data-bs-toggle="collapse" href="#collapseChangePass" role="button" aria-expanded="false"
               aria-controls="collapseExample">
                Сменить пароль
            </a>
        </div>
        <div class="collapse my-2" id="collapseChangePass">
            <!-- Старый пароль--------------------------------------------------------------------------------------------->
            <div class="form-group row my-2">
                <label class="col-sm-2 col-form-label">Старый пароль:</label>
                <div class="col-sm-5">
                    <input class="form-control ${(passwordOldError??)?string('is-invalid', '')}"
                           type="password" name="passwordOld"
                           placeholder="Введите старый пароль"/>
                    <#if passwordOldError??>
                        <div class="invalid-feedback">
                            ${passwordOldError}
                        </div>
                    </#if>
                </div>
            </div>
            <!-- Новый пароль --------------------------------------------------------------------------------------------->
            <div class="form-group row my-2">
                <label class="col-sm-2 col-form-label">Новый пароль:</label>
                <div class="col-sm-5">
                    <input class="form-control ${(passwordNewError??)?string('is-invalid', '')}"
                           type="password" name="passwordNew"
                           placeholder="Введите новый пароль"/>
                    <#if passwordNewError??>
                        <div class="invalid-feedback">
                            ${passwordNewError}
                        </div>
                    </#if>
                </div>
            </div>
            <!-- Подтверждение нового пароля ------------------------------------------------------------------------------>
            <div class="form-group row my-2">
                <label class="col-sm-2 col-form-label">Подтверждение:</label>
                <div class="col-sm-5">
                    <input class="form-control ${(passwordNew2Error??)?string('is-invalid', '')}"
                           type="password" name="passwordNew2"
                           placeholder="Подтвердите новый пароль"/>
                    <#if passwordNew2Error??>
                        <div class="invalid-feedback">
                            ${passwordNew2Error}
                        </div>
                    </#if>
                </div>
            </div>
        </div>
        <input type="hidden" name="_csrf" value="${_csrf.token}"/>
    </form>
    <#if message??>
        ${message}
    </#if>



</@c.page>