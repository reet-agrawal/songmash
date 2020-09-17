const MESSAGE_TYPE = {
    CONNECT: 0,
    UPDATEROOM: 1,
    GETPEOPLE: 2,
    BEGIN: 3,
    BEGINALL: 4,
    END: 5,
    FINISHED: 6,
    SELECTED: 7,
    USER_DONE: 8,
    PING: 9,
};

// change for Heroku
// const SOCKET_ADDRESS = "ws://localhost:4567/socket";
const SOCKET_ADDRESS = "wss://calm-plateau-83735.herokuapp.com/socket";

let conn;

let userid = getCookie("userid");

let myRoom = -1;
let myPeople = [];

$(document).ready(() => {

    // ping the server periodically
    setInterval(function () {
            pingServer();
        }, 3000
    );


    // Get the modal
    let modal = document.getElementById('myModal');
    let modalTitle = document.getElementById('label3');
    let startBtn = document.getElementById('start-btn-choose');

    if (!$(location).attr('pathname').includes('/results')) {
        // Remove the modal when the start button is clicked

        startBtn.onclick = function () {
            modal.style.display = "none";
        };
    }

    //set room number
    myRoom = parseInt($(location).attr('pathname').split('/room/').pop());

    let roomName = $(location).attr('pathname').split('/room/').pop().split('/')[0];
    modalTitle.innerHTML = `Users in Room ${roomName}`;

    if ($(location).attr('pathname').includes('/room/')) {
        console.log('room.js');
        // if user hits done
        $("#done").click(function () {
            endGame()
        });

        $("#start-btn-choose").click(function () {
            beginGame()
        });

        $("#btnleft").click(function () {
            const id1 = $("#frameOne").attr("src").split('/track/').pop();
            const id2 = $("#frameTwo").attr("src").split('/track/').pop();

            songSelected(id1, id2)
        });

        $("#btnright").click(function () {
            const id1 = $("#frameOne").attr("src").split('/track/').pop();
            const id2 = $("#frameTwo").attr("src").split('/track/').pop();

            songSelected(id2, id1)
        })
    }

    //change this line when we deploy on heroku
    conn = new WebSocket(SOCKET_ADDRESS);

    conn.onerror = err => {
        console.log('Connection error:', err);
    };


    conn.onmessage = msg => {
        const data = JSON.parse(msg.data);
        switch (data.type) {
            default:
                console.log(data);
                console.log('Unknown message type!', data.type);
                break;
            case MESSAGE_TYPE.CONNECT:
                console.log('SOMEONE CONNECTED');
                connectToRoom(myRoom, myPeople, conn);
                break;
            case MESSAGE_TYPE.GETPEOPLE:
                // updates myPeople array only
                let peopleArr = data.people.substr(1, data.people.length - 2).split(',');

                if (myRoom === data.room) {
                    updatePeopleList(peopleArr, $('#user-list'));
                }
                break;
            case MESSAGE_TYPE.BEGINALL:
                // remove the modal for everyone
                if (myRoom === data.room) {
                    modal.style.display = "none";
                }
                break;
            case MESSAGE_TYPE.FINISHED:
                //show playlist results page
                if (myRoom === data.room) {
                    console.log('EVERYONE IS FINISHED');

                    let playlistIDs = JSON.parse(data.playlistLinks);
                    let myPlaylistLink = playlistIDs[getCookie("userid")];

                    setCookie("myplaylist", myPlaylistLink, 10);

                    // redirect to results when everyone is done
                    window.location.href = 'results';
                }
                break;
            case MESSAGE_TYPE.USER_DONE:
                if (myRoom === data.room) {
                    let unfinished = JSON.parse(data.unfinished_users);

                    if (!unfinished[getCookie("userid")]) {
                        // this user is done

                        let unfinishedUsers = [];
                        for (let userID in unfinished) {
                            unfinishedUsers.push(unfinished[userID]);
                        }

                        // hide the display button
                        $("#start-btn-choose").hide();
                        $("#label4").hide();
                        // change the modal title
                        modalTitle.innerHTML = `You are done! We are waiting for the following users to finish.`;

                        // add unfinished users to the modal list
                        updatePeopleList(unfinishedUsers, $('#user-list'));

                        modal.style.display = "block";
                    }
                }
        }
    };

    // disconnect
    window.onbeforeunload = () => {
        disconnect();
    }
});

const updatePeopleList = (peopleArr, list) => {
    myPeople = peopleArr;

    list.empty();

    myPeople.forEach(function (e) {
        list.append(`<li>${e}</li>`);
    });
};

const connectToRoom = (room, userIDList, conn) => {
    const ROOM = {
        type: MESSAGE_TYPE.UPDATEROOM,
        payload:
            {
                room: room,
                authcode: getCookie("authcode"), // give auth code to socket
                userid: getCookie("userid"), // get spotify user id
            }
    };

    setCookie("roomid", room, 1);
    conn.send(JSON.stringify(ROOM));
};

const songSelected = (id1, id2) => {
    // id1 is song selected, id2 is song not selected
    const SONG = {
        type: MESSAGE_TYPE.SELECTED,
        id1: id1,
        id2: id2,
        room: myRoom
    };
    conn.send(JSON.stringify(SONG))
};

const beginGame = () => {
    const GAME = {
        type: MESSAGE_TYPE.BEGIN,
        room: myRoom
    };
    conn.send(JSON.stringify(GAME))
};

const endGame = () => {
    const END = {
        type: MESSAGE_TYPE.END,
        userid: getCookie("userid"),
        room: myRoom
    };
    conn.send(JSON.stringify(END))
};

const disconnect = () => {
    const postParameters = {
        "userid": getCookie("userid"),
        "roomid": getCookie("roomid"),
    };

    $.post("/exitroom", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        console.log(responseObject);
    });
};

const pingServer = () => {
    const PING_MSG = {
        type: MESSAGE_TYPE.PING
    };
    console.log("pinged the server.");
    conn.send(JSON.stringify(PING_MSG));
};

