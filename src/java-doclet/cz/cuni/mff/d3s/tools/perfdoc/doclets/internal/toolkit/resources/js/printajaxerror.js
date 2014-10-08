/**
 * Reports an error occured in an AJAX communication.
 * @param xhr 
 * @param status
 * @param graphLocation div id where the error will be displayed
 * @param errorThrown
 * @param priority the priority of last sent request
 */
function printAjaxError(xhr, status, graphLocation, errorThrown, priority) {
	alert( "Sorry, there was a problem! Detailed information can be found in debugger console." );
	console.log( "Error:" + errorThrown );
	console.log( "Status: " + status );
	console.dir( xhr );
	if (priority < 2) {
		if (xhr.status == 0) { 
			$("#" + graphLocation + " .right .graph").text("Server is shut-down, or could not connect to it."); 
		} else { 
			$("#" + graphLocation + " .right .graph").text(xhr.status + ": " + xhr.responseText); 
		}
	} 
}
