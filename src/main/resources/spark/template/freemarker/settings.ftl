<#assign css>
    <link rel="stylesheet" href="css/songmash.css">
</#assign>

<#assign content>
    <div class="outer">
        <p>
            <span class="hr3">Song</span><span class="hr4">Mash</span>
        </p>
    </div>

    <p id="settingslabel">
        Settings
    </p>

        <div id="label1">
            <p id="label1"> Number of songs in final playlist </p>
        </div>
        <div id="input1">
            <input type="text" class="input1" name="numberOfSongs" id="textbox1" placeholder="Number of Songs"> <br><br><br>
        </div>

        <div id="label2">
            <p id="label2"> Would you like to hear only fresh music? </p>
        </div>
        <div id="obscurity">
            <form>
            <input type="radio" name="obscurity" value="notObscure"> No, I like my comfort zone<br><br>
            <input type="radio" name="obscurity" value="obscure"> Sure, I would love to!<br><br>
            </form>
        </div>
    <form method = "GET" action="/home" id="settings-save">
        <button type="submit" id="save">Save</button>
    </form>
</#assign>

<#include "main.ftl">



