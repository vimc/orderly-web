<#-- @ftlvariable name="authProvider" type="String" -->
<#-- @ftlvariable name="montaguApiUrl" type="String" -->
<script type="text/javascript" src="/js/lib/jquery.slim.min.js"></script>
<#if scripts??>
    <@scripts></@scripts>
</#if>

<script>
    $("#logout-link").click(() => {
<#if authProvider?lower_case == "montagu">
        fetch("${montaguApiUrl}/logout/", {
            credentials: 'include'
        }).then((response) => {
            window.location.href = "/logout";
        });
<#else>
        window.location.href="/logout";
</#if>
    });
</script>

</body>
</html>