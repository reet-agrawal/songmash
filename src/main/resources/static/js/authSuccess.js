function createAuthCookie() {
    let url_string = window.location.href;

    let url = new URL(url_string);
    let code = url.searchParams.get("code");

    setCookie("authcode", code, 1);
    setCookie("authtime", Date.now(), 1);

    $.get("/getuserid", {}, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        if (responseObject.userID) {
            console.log('set user id cookie to ' + responseObject.userID);
            setCookie("userid", responseObject.userID);
        }
    });
}

$(document).ready(() => {
    // call when auth page is loaded
    if ($(location).attr('pathname').includes('/auth')) {
        createAuthCookie();
    }
});