/**
 * Sends measure request to a measuring server.
 * @param {JSON} requestData with informations for measuring (fields: testedMethod, generator, rangeValue, priority, id)
 * @param {Object} graphInfo Object with three fields: graph (Dygraph instance), xAxisLabel and divLocation (the id of the div where the messages are displayed)
 * @param {Number} priority the priority with which to send the request
 */
function callServer(requestData, graphInfo, priority) {
	$.ajax({
		url: "$serverAddress",
		data: requestData,
		type: "POST",
		success: function(json) {
			successFunction(json, requestData, graphInfo);
		},
		error: function( xhr, status, errorThrown ) {
			printAjaxError(xhr, status, graphInfo.divLocation, errorThrown, priority); 
		}
	});
}