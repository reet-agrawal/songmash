
$(document).ready(() => {

    let form = $('#settings-save');

    // form.onSubmit(function(event))

    $("#save").click(function(){
        console.log('hit');
        sendSettings()
    });
});

function sendSettings(){
    let numSongs = document.getElementById("textbox1").value;
    let obscurity = $("input[name='obscurity']").val();
    console.log(numSongs);
    console.log(obscurity);
     postParameters = {"numSongs": numSongs , "obscurity": obscurity}


    $.post("/settings", postParameters, responseJSON => {
        
    });
}