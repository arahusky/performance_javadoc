/**
 * Function that is called when we get possitive respond from server. This function parses respond, shows measured results and when needed calls server again.
 * @param {JSON} respondData the JSON returned from the server with fields: data (measured data), units
 * @param {string} requestData with informations for measuring (fields: testedMethod, generator, rangeValue, priority, id)
 * @param {Object} graphInfo Object with three fields: graph (Dygraph instance), xAxisLabel and divLocation (the id of the div where the messages are displayed)
 */
 function successFunction (respondData, requestData, graphInfo) {

 var jsonData = JSON.parse(requestData); 

 //get priority of the request
 var priority = jsonData.priority;

 //incrementing priority in the requesting JSON
 jsonData.priority++;
 var newData = JSON.stringify(jsonData, null, 2);

 if (priority == 1) {
        //creating new Dygraph
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
        callServer(newData, graphInfo, ++priority); 
    } else if (priority < 4) {
        var graph = graphInfo.graph;
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
      console.dir(respondData);
      //priority is now 4
      graph.updateOptions( { 'file': JSON.parse(respondData).data } );
      //graph.updateOptions( { 'colors': ['#0000FF'] });
      graph.updateOptions( { 'strokeWidth': 1.25 });
      graph.updateOptions( { 'strokePattern': null })
  }
}