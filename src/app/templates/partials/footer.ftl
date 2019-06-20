<#-- @ftlvariable name="montaguApiUrl" type="String" -->
<script type="text/javascript" src="/js/lib/jquery.slim.min.js"></script>
<script>var appUrl="${appUrl}"</script>
<script type="text/javascript" src="${appUrl}/js/lib/jquery.slim.min.js"></script>r
<#if scripts??>
    <@scripts></@scripts>
</#if>
<#if authProvider?lower_case == "montagu">
<script>
function logoutViaMontagu() {
    fetch("${montaguApiUrl}/logout/", {
        credentials: 'include'
    }).then(() => {
        window.location.href = "${appUrl}/logout";
    });
}
</script>
</#if>
</body>
</html>