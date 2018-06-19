var header = document.querySelector('header');
var baseUrl = header.getAttribute('data-auth-base');
var path = header.getAttribute('data-auth-path');

var xhr = new XMLHttpRequest();
xhr.open('GET', baseUrl + path);
xhr.withCredentials = true;
xhr.send(null);

xhr.onreadystatechange = function () {
    var DONE = 4; // readyState 4 means the request is done.
    var OK = 200;
    if (xhr.readyState === DONE) {
        if (xhr.status === OK) {
            var response = JSON.parse(xhr.responseText);
            if((typeof(response.id) === 'undefined')) {
                return false;
            }
            var id = response.id;
            var name = response.name;
            appendUserIdToUrls(id);
            addNameToProfileButton(name);
            showAuthenticatedHeader();
        }
    }
};

function showAuthenticatedHeader() {
    var header = document.querySelector('header');
    header.setAttribute('data-authenticated', 'true');
}

function appendUserIdToUrls(userId) {
    var links = document.querySelectorAll(".append-uid");
    for (var i = 0; i < links.length; i++) {
        var link = links[i];
        var href = link.getAttribute('href');
        var newHref = href + userId;
        link.setAttribute('href', newHref);
    }
}

function addNameToProfileButton(name) {
    var profileButtonSpan = document.querySelector('header .user-menu .profile span:first-child');
    profileButtonSpan.innerHTML = name;
}