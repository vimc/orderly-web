<#-- @ftlvariable name="montaguUrl" type="String" -->
<script>var appUrl="${appUrl}"</script>
<script type="text/javascript" src="${appUrl}/js/lib/jquery.slim.min.js"></script>
<#if scripts??>
    <@scripts></@scripts>
</#if>
<#if authProvider?lower_case == "montagu">
<script>
function logoutViaMontagu() {
    fetch("${montaguUrl}/api/v1/logout/", {
        credentials: 'include'
    }).then(() => {
        window.location.href = "${appUrl}/logout";
    });
}
</script>
</#if>
</body>
</html>