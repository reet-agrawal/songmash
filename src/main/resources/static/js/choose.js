let songOne = "";
let songTwo = "";
const defaultURL = "https://open.spotify.com/embed/track/";

$(document).ready(() => {
    getFirstSongs();

    $("#btnleft").click(function () {
        getNextSongs(true)
    });

    $("#btnright").click(function () {
        getNextSongs(false)
    });
});

function getNextSongs(isLeftClicked) {
    let postParameters = {};

    if (isLeftClicked) {
        postParameters = {songPicked: songOne, songNotPicked: songTwo}
    } else {
        postParameters = {songPicked: songTwo, songNotPicked: songOne}
    }

    $.post("/newresults", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        songOne = responseObject.songOne;
        songTwo = responseObject.songTwo;
        document.getElementById("frameOne").src = defaultURL + songOne;
        document.getElementById("frameTwo").src = defaultURL + songTwo;
    });
}

function getFirstSongs() {

    $.post("/choices", responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        songOne = responseObject.songOne;
        songTwo = responseObject.songTwo;

        let frameOne = document.getElementById("frameOne");
        let frameTwo = document.getElementById("frameTwo");

        if (frameOne) {
            frameOne.src = defaultURL + songOne;
        }

        if (frameTwo) {
            frameTwo.src = defaultURL + songTwo;
        }
    });
}
