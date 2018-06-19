var hamburgerButton = document.querySelector(".user-menu a.hamburger");
var profileButton = document.querySelector(".user-menu a.profile");
var mainMenu = document.querySelector(".main-menu ul");
var authMenu = document.querySelector(".auth-menu");

hamburgerButton.addEventListener('click', function() {
    var openAttribute = mainMenu.getAttribute('data-open');
    if (openAttribute === "true") {
        mainMenu.setAttribute('data-open', 'false');
    } else {
        mainMenu.setAttribute('data-open', 'true');
    }
});

profileButton.addEventListener('click', function() {
    var openAttribute = authMenu.getAttribute('data-open');
    if (openAttribute === "true") {
        authMenu.setAttribute('data-open', 'false');
    } else {
        authMenu.setAttribute('data-open', 'true');
    }
});