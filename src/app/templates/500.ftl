<#-- @ftlvariable name="errors" type="kotlin.collections.Iterable<org.vaccineimpact.orderlyweb.models.ErrorInfo>" -->
<@layout>
    <h1>Something went wrong</h1>
   <ul>
    <#list errors as error>
        <li>${error.detail}</li>
    </#list>
   </ul>
</@layout>
