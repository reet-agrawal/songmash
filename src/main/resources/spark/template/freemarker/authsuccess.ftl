<#assign content>

    <div class="outer">
        <p>
            <span class="hr3">Song</span><span class="hr4">Mash</span>
        </p>
    </div>

<p id="auth">
    <#if success>
        success
        ${userID}
    <#else>
        failure
    </#if>
</p>

<form method="GET" action="/home">
<button type="submit" id="home">Back to Home</button>
</form>
</#assign>
<#include "main.ftl">