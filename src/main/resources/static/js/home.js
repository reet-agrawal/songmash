$(document).ready(() => {
    let createRoomForm = $('#create-room-form');
    let joinRoomBtn = $('#join-room-btn');
    let logoutBtn = $('#logout-site-btn');

    let roomID = '';

    for (let i = 0; i < 4; i++) {
        roomID += `${genRandDigit()}`;
    }

    createRoomForm.attr('action', `/room/${roomID}/choose`);

    // if user clicks join room.
    joinRoomBtn.click(function () {
        let roomID = promptForRoomID();

        if (roomID) {
            window.location.href = `/room/${roomID}/choose`;
        }
    });

    // if user clicks logout
    logoutBtn.click(function () {
        let userID = getCookie("userid");
        setCookie("authcode", "", -100);
        setCookie("authtime", "", -100);

        // send post request to tell server to remove hash
        $.post("/logout", {"userid": userID}, responseJSON => {
            const responseObject = JSON.parse(responseJSON);
            console.log(responseObject);
        });

        // redirect home
        window.location.href = `/home`;
    });
});

function genRandDigit() {
    return Math.floor(Math.random() * 10);
}

function promptForRoomID() {
    return prompt("Enter the Room Code: ", "");
}

