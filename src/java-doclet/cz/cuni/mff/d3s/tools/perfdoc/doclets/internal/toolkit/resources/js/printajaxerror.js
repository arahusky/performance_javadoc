function printAjaxError(xhr, status, graphName, errorThrown, priority) {
	alert( "Sorry, there was a problem! Detailed information can be found in debugger console." );
	console.log( "Error:" + errorThrown );
	console.log( "Status: " + status );
	console.dir( xhr );
	if (priority < 2) {
		if (xhr.status == 0) { 
			$("#" + graphName + " .right .graph").text("Server is shut-down, or could not connect to him."); 
		} else { 
			$("#" + graphName + " .right .graph").text(xhr.status + ": " + xhr.responseText); 
		}
	} 
}
