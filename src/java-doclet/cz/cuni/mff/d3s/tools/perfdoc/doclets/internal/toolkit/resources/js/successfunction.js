/**
 * Function that is called when we get possitive respond from server. This function parses respond, shows measured results and when needed calls server again.
 * @param {JSON} respondData the JSON returned from the server with fields: data (measured data), units
 * @param {string} requestData with informations for measuring (fields: testedMethod, generator, rangeValue, priority, id)
 * @param {Object} graphInfo Object with three fields: graph (Dygraph instance), xAxisLabel and divLocation (the id of the div where the messages are displayed)
 */
 function successFunction (respondData, requestData, graphInfo) {

 var jsonRequestData = JSON.parse(requestData); 
 var jsonRespondData = JSON.parse(respondData);

 //get priority of the request
 var priority = jsonRespondData.priority;

 //if we requested results with lowest priority, we must firstly create graph
 if ((jsonRequestData.priority == 1) && (graphInfo.graph == null)) {
  graphInfo.graph = new Dygraph(
            document.getElementById(graphInfo.divLocation).getElementsByClassName("right")[0].getElementsByClassName("graph")[0], 
            JSON.parse(respondData).data,
            {
                ylabel: 'Elapsed time (' + JSON.parse(respondData).units + ')',
                xlabel: graphInfo.xAxisLabel,
                strokeWidth: 0.5,
                //colors: ['#FF82AB'],
                strokePattern: Dygraph.DASHED_LINE,
                drawPoints : true, 
                pointSize : 2,
                labels: [graphInfo.xAxisLabel,"Mean","Median"],
            }
            );
 }

  //setting priority to respondData.priority + 1
 jsonRequestData.priority = priority + 1;
 var newData = JSON.stringify(jsonRequestData, null, 2);

 var graph = graphInfo.graph;

 if (priority == 1) {
        callServer(newData, graphInfo, ++priority); 
    } else if (priority < 4) {        
        //setting data to be plotted         
        graph.updateOptions( { 'file': JSON.parse(respondData).data} );

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
      graph.updateOptions( { 'file': JSON.parse(respondData).data } );
      //graph.updateOptions( { 'colors': ['#0000FF'] });
      graph.updateOptions( { 'strokeWidth': 1.25 });
      graph.updateOptions( { 'strokePattern': null })
  }
}