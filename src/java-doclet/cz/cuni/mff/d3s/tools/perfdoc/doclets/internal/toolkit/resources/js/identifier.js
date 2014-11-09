//code that generates/loads the identifier of the user, which is sent with the AJAX request to the server
var globalIdentifier;
var cookieName = "perfJavaDocUID";
if (navigator.cookieEnabled) {
	//if cookies are enabled, we first check, whether cookie with name perfJavaDocUID exists
	var pom = getCookie(cookieName);
	if (pom == "") {
		globalIdentifier = Math.random().toString(36).substring(7);
		setCookie(cookieName, globalIdentifier);
	} else {
		globalIdentifier = pom;
	}
} else {
	globalIdentifier = Math.random().toString(36).substring(7);
}

/**
 * Returns cookie that belongs to the given name. If no such cookie exists, returns empty String.
 * @param {string} cname the name of the requested cookie 
 */
function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i=0; i<ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1);
        if (c.indexOf(name) != -1) return c.substring(name.length,c.length);
    }
    return "";
}

/**
 * Sets cookie with given name, value and expiration.
 * @param {string} cname 
 * @param {string} cvalue 
 * @param {number} exdays the expiration in days.
 */
function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires="+d.toUTCString();
    document.cookie = cname + "=" + cvalue + "; " + expires;
}