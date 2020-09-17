const first = '<iframe src=\"https://open.spotify.com/embed/track/';
const last = '\" width=\"250\" height=\"250\" frameborder=\"0\" allowtransparency=\"true\" allow=\"encrypted-media\">';

$(document).ready(() => {
    const postParameters = {};
    $.post("/playlist", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        const ids = responseObject.playlist;
        for (let i = 0; i < ids.length; i++) {
            const finale = first + ids[i] + last;
            $(document.body).append(finale);
        }
    });

    // get the playlist cookie
    let playlistLink = getCookie("myplaylist");
    let playlistButton = $('#playlist-spotify-btn');
    playlistButton.attr("href", playlistLink);
});