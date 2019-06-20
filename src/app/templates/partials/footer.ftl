<#-- @ftlvariable name="authProvider" type="String" -->
<#-- @ftlvariable name="montaguApiUrl" type="String" -->
<script type="text/javascript" src="/js/lib/jquery.slim.min.js"></script>
<#if scripts??>
    <@scripts></@scripts>
</#if>
<#if authProvider?lower_case == "montagu">
<script>
function logoutViaMontagu() {
    fetch("${montaguApiUrl}/logout/", {
        credentials: 'include'
    }).then(() => {
        window.location.href = "/logout";
    });
}
</script>
</#if>
</body>
</html>