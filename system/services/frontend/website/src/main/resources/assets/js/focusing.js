var elementsWithFocusChildren = document.querySelectorAll(".focus-children");

for (var i = 0; i < elementsWithFocusChildren.length; i++) {
    var element = elementsWithFocusChildren[i];
    var children = element.getElementsByTagName("*");
    for (var j = 0; j < children.length; j++) {
        var child = children[j];
        child.onblur = function() {
            element.setAttribute('focused', 'false');
        };
        child.onfocus = function() {
            element.setAttribute('focused', 'true');
        };
    }
}