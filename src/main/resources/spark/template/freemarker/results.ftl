<#assign css>
<link rel="stylesheet" href="css/results.css">
</#assign>

<#assign content>
<div id="list">
</div>

<form class="form">
    <a class="fakeBtn" role="button" target="_blank" id="playlist-spotify-btn">Open in Spotify</a>
</form>

<form method="GET" action="/home">
	<button type="submit" id="home">Back to Home</button>
</form>

</#assign>
<#include "main.ftl">
<script src="/js/results.js"></script>
