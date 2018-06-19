var imagesToLoad = document.querySelectorAll("img[data-lazy-src]");
for (var i = 0; i < imagesToLoad.length; i++) {
    var image = imagesToLoad[i];
    var imageUrl = image.getAttribute('data-lazy-src');

    image.setAttribute('data-loaded', 'false');
    image.onload = function () {
        this.setAttribute('data-loaded', 'true');
    };
    image.setAttribute('src', imageUrl);
    image.removeAttribute('data-lazy-src');
}