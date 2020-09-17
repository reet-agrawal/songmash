<#assign css>
    <link rel="stylesheet" href="css/choose.css">
</#assign>

<#assign content>
    <div class="outer">
        <p>
            <span class="hr3">Song</span><span class="hr4">Mash</span>
        </p>
    </div>


    <div id="myModal" class="modal">
        <div class="modal-content">
            <p id="label3"></p>
            <ul id="user-list">
                <li>Loading users...</li>
            </ul>
            <button id="start-btn-choose" class="blackButton"> Begin for all players</button>
            <p id="label4">Please wait until your username appears to begin or invite other players!</p>
            <ul id="user-list"></ul>
        </div>
    </div>

    <p class="desc2">
        Choose the song you like more (Or hate less). You can press done at any point!
    </p>

    <div class="frame1">
        <iframe id="frameOne" src="" width="550"
                height="430" frameborder="0" allowtransparency="true" allow="encrypted-media"></iframe>
    </div>

    <div class="frame2">
        <iframe id="frameTwo" src="" width="550"
                height="430" frameborder="0" allowtransparency="true" allow="encrypted-media"></iframe>
    </div>

    <button type="submit" id="btnleft">Song 1</button>
    <button type="submit" id="btnright">Song 2</button>

    <button type="submit" id="done"> Done</button>

    <div>
    </div>
</#assign>
<#include "main.ftl">
<script type="module" src="/js/websockets.js"></script>