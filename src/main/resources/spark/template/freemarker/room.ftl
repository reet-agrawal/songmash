<#assign content>
    <div class="outer">
        <p>
            <span class="hr3">Song</span><span class="hr4">Mash</span>
        </p>
    </div>

    <p class="desc">
        Welcome to SongMash Room! Have your friends join at room id 1234!
    </p>

    <p id="label1">
    	Members in this room:
    </p>
    <ol type="1" id="memberList">
    </ol>

    <form action="/room/1234/choose">
        <button id="start"> Begin for all members</button>
    </form>

</#assign>

<script type="module" src="js/websockets.js"></script>
<#include "main.ftl">





