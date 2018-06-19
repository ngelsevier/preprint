// Email obfuscator script 2.1 by Tim Williams, University of Arizona
// Random encryption key feature coded by Andrew Moulden
// This code is freeware provided these four comment lines remain intact
// A wizard to generate this code is at http://www.jottings.com/obfuscator/

var ltr;
var coded = "MXbbd4e@MM4q.gdZ";
var key = "H3tBI14b5LTUPFdiRseNncWh2CwOy6xASMEYqgfujDzmlKVak79vrp0XG8QZoJ";
var shift = coded.length;
var link = "";
var readmePanel = document.querySelector('.readme .panel');
for (var i = 0; i < coded.length; i++) {
    if (key.indexOf(coded.charAt(i)) == -1) {

        ltr = coded.charAt(i);
        link += (ltr);
    }
    else {
        ltr = (key.indexOf(coded.charAt(i)) - shift + key.length) % key.length;
        link += (key.charAt(ltr));
    }
}
var markup = "<p>As we look to improve your on-site search experience, we welcome your feedback on this feature by email at ";
markup += "<a href='mailto:" + link + "'>" + link + "</a>.</p>";
readmePanel.innerHTML += markup;