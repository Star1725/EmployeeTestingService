<!--Этот макрос называется 'login_logout':
    - он содержит два подмакроса: один для входа (login), другой для выхода (logout).
    - 'login' имеет переменную 'path'-->
<#macro login path isRegisterForm>
    <form action="${path}" method="post" class="mt-2">
        <div class="form-group row my-2">
            <label class="col-sm-2 col-form-label">ФИО пользователя:</label>
            <div class="col-sm-5">
                <input type="text" name="username"
                       class="form-control ${(usernameError??)?string('is-invalid', '')}"
                       value="<#if user??>${user.username}</#if>"
                       placeholder="Введите ФИО"/>
                <#if usernameError??>
                    <div class="invalid-feedback">
                        ${usernameError}
                    </div>
                </#if>
            </div>
        </div>
        <div class="form-group row my-2">
            <label class="col-sm-2 col-form-label">Пароль:</label>
            <div class="col-sm-5">
                <input class="form-control ${(passwordError??)?string('is-invalid', '')}"
                       type="password" name="password"
                       placeholder="Введите пароль"/>
                <#if passwordError??>
                    <div class="invalid-feedback">
                        ${passwordError}
                    </div>
                </#if>
            </div>
        </div>
        <#if isRegisterForm>
            <div class="form-group row my-2">
                <label class="col-sm-2 col-form-label">Подтвердите Пароль:</label>
                <div class="col-sm-5">
                    <input class="form-control ${(password2Error??)?string('is-invalid', '')}"
                           type="password" name="password2"
                           placeholder="Введите пароль ещё раз"/>
                    <#if password2Error??>
                        <div class="invalid-feedback">
                            ${password2Error}
                        </div>
                    </#if>
                </div>
            </div>
            <button class="btn btn-primary col-sm-2" type="submit">
                Создать
            </button>
        <#else>
            <button class="btn btn-primary col-sm-2" type="submit">
                Вход
            </button>
        </#if>
        <#if !isRegisterForm>
            <div class="my-2">
                <a href="/registration">Регистрация</a> нового пользователя
            </div>
        </#if>
        <input type="hidden" name="_csrf" value="${_csrf.token}"/>
    </form>
</#macro>