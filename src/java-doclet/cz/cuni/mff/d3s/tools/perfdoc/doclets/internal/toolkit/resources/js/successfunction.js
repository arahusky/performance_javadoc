/**
 * Function that is called when we get possitive respond from server. This function parses respond, shows measured results and when needed calls server again.
 * @param {JSON} respondData the JSON returned from the server with fields: data (measured data), units
 * @param {string} requestData with informations for measuring (fields: testedMethod, generator, rangeValue, priority, id)
 * @param {Object} graphInfo Object with three fields: graph (Dygraph instance), xAxisLabel and divLocation (the id of the div where the messages are displayed)
 */
 function successFunction (respondData, requestData, graphInfo) {

 var jsonRequestData = JSON.parse(requestData); 
 var jsonRespondData = JSON.parse(respondData);

 //priority of the request
 var priority = jsonRespondData.priority;
 
 var divWhereGraphShouldBePlaced = document.getElementById(graphInfo.divLocation).getElementsByClassName("right")[0].getElementsByClassName("graph")[0];
 
 //width of graph
 var width = divWhereGraphShouldBePlaced.offsetWidth * (9/10);
 //height of graph
 var height = width * 3/4;

 //if we requested results with lowest priority, we must create graph at first
 if ((jsonRequestData.priority == 1) && (graphInfo.graph == null)) {
  graphInfo.graph = new Dygraph(
            divWhereGraphShouldBePlaced, 
            jsonRespondData.data,
            {
                ylabel: 'Elapsed time (' + jsonRespondData.units + ')',
                xlabel: graphInfo.xAxisLabel,
                strokeWidth: 0.5,
                //colors: ['#FF82AB'],
                strokePattern: Dygraph.DASHED_LINE,
                drawPoints : true, 
                pointSize : 2,
                //height and width must be explicitly set (otherwise there are problems while changing table output to graph output)
                height : height,
                width : width,
                labels: [graphInfo.xAxisLabel,"Mean","Median"],
            }
            );
 }

  //setting priority to respondData.priority + 1
 jsonRequestData.priority = priority + 1;
 var newData = JSON.stringify(jsonRequestData, null, 2);

 //generating and placing table into its div
 $("#" + graphInfo.divLocation + " .right .table").html(generateTable(graphInfo.xAxisLabel, jsonRespondData.units, jsonRespondData.data)); 

 var graph = graphInfo.graph;

 if (priority == 1) {
        callServer(newData, graphInfo, ++priority); 
    } else if (priority < 4) {        
        //setting data to be plotted         
        graph.updateOptions( { 'file': jsonRespondData.data} );
        graph.updateOptions( { 'ylabel': 'Elapsed time (' + jsonRespondData.units + ')' } );

        if (priority == 2) {
            //graph.updateOptions( { 'colors': ['#B0171F'] });
            graph.updateOptions( { 'strokeWidth': 0.75 });
        } else {
            //graph.updateOptions( { 'colors': ['#9400D3'] }); 
            graph.updateOptions( { 'strokeWidth': 1.0 }); 
        }

        callServer(newData, graphInfo, ++priority);
    } else {
      var graph = graphInfo.graph;
      //priority is now 4
      graph.updateOptions( { 'file': jsonRespondData.data } );
      graph.updateOptions( { 'ylabel': 'Elapsed time (' + jsonRespondData.units + ')' } );
      //graph.updateOptions( { 'colors': ['#0000FF'] });
      graph.updateOptions( { 'strokeWidth': 1.25 });
      graph.updateOptions( { 'strokePattern': null })
  }
}

/**
 * Function that returns html representing output as HTML table.
 * @param {string} xAxisLabelName title, that describes against what attribute is the measurement performed
 * @param {string} units describes units, in which the results are
 * @param {Array} data array containing measured results, where i-th index looks like [measuredPoint, mean, median]
 */
function generateTable(xAxisLabelName, units, data) {
  var res = '<table border="1" class = "resultTable">';
  res += '<thead><tr><th>' + xAxisLabelName + '</th><th>Elapsed time mean (' + units + ')</th><th>Elapsed time median (' + units + ')</th></tr></thead><tbody>';

  for (i = 0; i < data.length; i++) {
    var measuredPoint = data[i][0];
    var mean = data[i][1];
    var median = data[i][2];
    res += "<tr>";
    res += "<td>" + measuredPoint + "</td>";
    res += "<td>" + mean + "</td>";
    res += "<td>" + median + "</td>";
    res += "</tr>"
  }

  res += '</tbody></table>';

  return res;
}