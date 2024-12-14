<#import "parts/common.ftl" as c>
<#include "parts/security.ftl">

<@c.page>

    <#if user.id == idUserContext>
        <h3 class="mb-2">Ваш профиль</h3>
    <#else >
        <h3 class="mb-2">Профиль пользователя ${user.username}</h3>
    </#if>
    <form method="post" class="mt-2">
        <!-- Имя пользователя ------------------------------------------------------------------------------------->
        <div class="form-group row my-3">
            <label class="col-sm-2 col-form-label">Имя пользователя:</label>
            <div class="col-sm-5">
                <input class="form-control ${(usernameNewError??)?string('is-invalid', '')}"
                       type="text" name="usernameNew"
                       value="<#if user??>${user.username}</#if>"
                       placeholder="ФИО"/>
                <#if usernameNewError??>
                    <div class="invalid-feedback">
                        ${usernameNewError}
                    </div>
                </#if>

            </div>
        </div>
        <!-- Организация ---------------------------------------------------------------------------------------------->
<#--        <div class="form-group row my-2">-->
<#--            <label class="col-sm-2 col-form-label">Организация:</label>-->
<#--            <div class="col-sm-5">-->
<#--                <select class="form-select form-select-sm-2 ${(organizationName_SelectedError??)?string('is-invalid', '')}"-->
<#--                        name="organizationName_Selected"-->
<#--                        aria-label=".form-select-sm-2">-->
<#--                    <option selected>-->
<#--                        <#if user.organization??>${user.organization.organizationName}-->
<#--                        <#else>-->
<#--                        </#if>-->
<#--                    </option>-->
<#--                    <#list organizations! as organization>-->
<#--                        <option value="${organization.organizationName}"-->
<#--                                name="organizationName_Selected">${organization.organizationName}</option>-->
<#--                    </#list>-->
<#--                </select>-->
<#--                <#if organizationName_SelectedError??>-->
<#--                    <div class="invalid-feedback">-->
<#--                        ${organizationName_SelectedError}-->
<#--                    </div>-->
<#--                </#if>-->
<#--            </div>-->
<#--        </div>-->
<#--        <!-- Администратор подразделения / подразделение ------------------------------------------------------------------------------------------&ndash;&gt;-->
<#--        <#if !user.isMainAdmin()>-->
<#--            <div class="form-group row my-2">-->
<#--                <label class="col-sm-2 col-form-label">-->
<#--                    <#if user.isAdmin()>-->
<#--                        Администратор подразделения:-->
<#--                    <#elseif user.isUser()>-->
<#--                        Подразделение:-->
<#--                    </#if>-->
<#--                </label>-->
<#--                <div class="col-sm-5">-->
<#--                    <select class="form-select form-select-sm-2 ${(divisionName_SelectedError??)?string('is-invalid', '')}"-->
<#--                            name="divisionName_Selected"-->
<#--                            aria-label=".form-select-sm-2">-->
<#--                        <option selected>-->
<#--                            <#if user.division??>${user.division.divisionName}-->
<#--                            </#if>-->
<#--                        </option>-->
<#--                        <#list divisions! as division>-->
<#--                            <option value="${division.divisionName}"-->
<#--                                    name="divisionName_Selected">${division.divisionName}</option>-->
<#--                        </#list>-->
<#--                    </select>-->
<#--                    <#if divisionName_SelectedError??>-->
<#--                        <div class="invalid-feedback">-->
<#--                            ${divisionName_SelectedError}-->
<#--                        </div>-->
<#--                    </#if>-->
<#--                </div>-->
<#--            </div>-->
<#--        </#if>-->
        <!-- Роль ------------------------------------------------------------------------------------------------->
        <#if isAdmin>
            <div class="form-group row my-3">
                <label class="col-sm-2 col-form-label">Роль:</label>
                <div class="col-sm-5">
                    <#list roles as role>
                        <div>
                            <label>
                                <input class="form-check-input" type="checkbox" name="${role}"
                                        <#if user.id == idUserContext>
                                            ${user.roles?seq_contains(role)?string("checked disabled", "disabled")}
                                        <#else>
                                            <#if rolesUserContext?seq_contains(role)>
                                                <#if role == "MAIN_ADMIN"> disabled
                                                <#else>
                                                    <#if user.roles?seq_contains(role)>checked
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
                                        <#if user.id == idUserContext>
                                            ${user.accessLevels?seq_contains(level)?string("checked disabled", "disabled")}
                                        <#else >
                                            <#if levelsAccessUserContext?seq_contains(level)>
                                                <#if user.accessLevels?seq_contains(level)>checked
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
                                        <#if user.id == idUserContext>
                                            ${user.specAccesses?seq_contains(specAccess)?string("checked disabled", "disabled")}
                                        <#else >
                                            <#if specAccessUserContext?seq_contains(specAccess)>
                                                <#if user.specAccesses?seq_contains(specAccess)>checked
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
        <input type="hidden" name="usernameOld" value="${user.getUsername()}"/>
        <input type="hidden" name="id" value="${user.id}"/>
    </form>
    <#if message??>
        ${message}
    </#if>

</@c.page>