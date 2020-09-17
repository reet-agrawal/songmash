<#assign css>
    <link rel="stylesheet" href="css/songmash.css">
</#assign>

<#assign content>

    <div class="outer">
        <p>
            <span class="hr3">Song</span><span class="hr4">Mash</span>
        </p>
    </div>

    <p class="desc">
        Welcome to SongMash! This program will let you generate a Spotify playlist of songs that we think you would like
        based on your preferences! Just begin by signing into your Spotify account and answer a questions for us and
        we will take it from there! (You can adjust the length of your playlist in the settings menu!)
    </p>

    <#if isAuthorized>
        <button type="submit" id="join-room-btn">Join Room</button>
        <button type="submit" id="logout-site-btn">Logout</button>

        <form id="create-room-form">
            <button type="submit" id="create-room-btn">Create Room</button>
        </form>

    <#else>
        <form class="form">
            <a href="${authURL}" class="fakeBtn" role="button" id="auth-spotify-btn">Authorize</a>
        </form>
    </#if>


    <div id="settings" class="undecorated-text">
        <p style="font-size:18px;text-decoration:underline"><a href="/displaysettings" style="color:#1ED761">Settings</p>
    </div>



</#assign>
<#include "main.ftl">
<script src="/js/home.js"></script>




