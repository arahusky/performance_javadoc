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
 
 //div, where the canvas with graph will be shown
 var divForGraph = document.createElement('div');
 divForGraph.setAttribute('class', 'graphImage');
 
 //div for options to manipulate with graph 
 var divForGraphOptions = document.createElement('div');
 divForGraphOptions.setAttribute('class', 'graphOptions');
 divForGraphOptions.setAttribute('align', 'right');

  $("#" + graphInfo.divLocation + " .right .graph").empty();
  $("#" + graphInfo.divLocation + " .right .graph").append(divForGraph);
  $("#" + graphInfo.divLocation + " .right .graph").append(divForGraphOptions);

  graphInfo.graph = new Dygraph(
            divForGraph, 
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
                customBars: true,
                legend: 'always',
                labels: [graphInfo.xAxisLabel,"Mean","Median"]
            }
            );

    //checkbox to switch off/on the mean line
    var checkbox1 = document.createElement('input');
    checkbox1.type = "checkbox";
    checkbox1.setAttribute('checked', 'true');
    //the id of the "mean" checkbox is generated from the "main" div ant the suffix mean
    var checkbox1ID = graphInfo.divLocation + "mean";
    checkbox1.setAttribute('id', checkbox1ID);

    //checkbox to switch off/on the median line
    var checkbox2 = document.createElement('input');
    checkbox2.type = "checkbox";
    checkbox2.setAttribute('checked', 'true');
    //the id of the "median" checkbox is generated from the "main" div ant the suffix median
    var checkbox2ID = graphInfo.divLocation + "median";
    checkbox2.setAttribute('id', checkbox2ID);
    
    checkbox1.onclick=function() {
        if (graph.visibility()[0] == true) {
          graph.setVisibility(0,false); 
        } else {
          graph.setVisibility(0,true); }
    }

    checkbox2.onclick=function() {
        if (graph.visibility()[1] == true) {
          graph.setVisibility(1,false); 
        } else {
          graph.setVisibility(1,true); }
    }

    //label for mean-checkbox
    var meanLabel = document.createElement("label");
    meanLabel.setAttribute("for", checkbox1ID);
    meanLabel.appendChild(document.createTextNode("Mean"));
    divForGraphOptions.appendChild(meanLabel);    
    divForGraphOptions.appendChild(checkbox1);

    //label for median-checkbox
    var medianLabel = document.createElement("label");
    medianLabel.setAttribute("for", checkbox2ID);
    medianLabel.appendChild(document.createTextNode("Median"));
    divForGraphOptions.appendChild(medianLabel);
    divForGraphOptions.appendChild(checkbox2);


    generateRadios(graphInfo.graph, graphInfo.divLocation, divWhereGraphShouldBePlaced);
 }

  //setting priority to respondData.priority + 1
 jsonRequestData.priority = priority + 1;
 var newData = JSON.stringify(jsonRequestData, null, 2);

 //generating and placing table into its div
 $("#" + graphInfo.divLocation + " .right .table").html(generateTable(graphInfo.xAxisLabel, jsonRespondData.units, jsonRespondData.data)); 

 var graph = graphInfo.graph;
 graph.updateOptions( { 'yAxisLabelWidth': getWidthYLabel(jsonRespondData.data)} );

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
  res += '<thead><tr>';
  res += '<th>' + xAxisLabelName + '</th>';
  res += '<th>Elapsed time mean (' + units + ')</th>';
  res += '<th>Std. deviation</th>';
  res += '<th>Q1, Q2, Q3 (' + units + ')</th>';
  res += '</tr></thead><tbody>';

  for (i = 0; i < data.length; i++) {
    var measuredPoint = data[i][0];
    var mean = data[i][1][1];
    var stdDeviation = mean - data[i][1][0];
    var q1 = data[i][2][0];
    var median = data[i][2][1];
    var q3 = data[i][2][2];
    res += "<tr>";
    res += "<td>" + measuredPoint + "</td>";
    res += "<td>" + mean + "</td>";
    res += "<td>" + stdDeviation + "</td>";
    res += "<td> " + q1 + "," + median + "," + q3 + "</td>";
    res += "</tr>"
  }

  res += '</tbody></table>';

  return res;
}


/**
 * Generates radio buttons to switch between graph and tabular results.
 * @param {Object} graph instance of graph
 * @param {string} divString name of div, where should the graph be generated
 * @param {Object} divElement DOM element representing div of graph area
 */
function generateRadios(graph, divString, divElement) {
    var radiosName = divString + "_radios";
    var graphOptionID = radiosName + "Graph";
    var tableOptionID = radiosName + "Table";

    //if there is no content in the div
    if( $("#" + divString + " .right .radio").text().length == 0)  {
    var code = '<form id="' + radiosName +'"> <input type="radio" id ="' + graphOptionID + '" name="' + radiosName + '" value="graph" checked="checked">'
                + '<label for="'+ graphOptionID + '"> Graph</label>'
                + '<input type="radio" id ="' + tableOptionID + '" name="' + radiosName + '" value="table" >'
                + '<label for="' + tableOptionID + '"> Table</label>'
                + '</form>';

    $("#" + divString + " .right .radio").html(code);
    }

    $("#" + radiosName).click(function() {
       var value = $('input:radio[name="' + radiosName + '"]:checked').val();
       if (value == 'graph') {
          $("#" + divString + " .right .graph").show();
          $("#" + divString + " .right .table").hide();

          //new width
          var width = divElement.offsetWidth * (9/10);
          //height of graph
          var height = width * 3/4;
          //we need to resize the graph (otherwise he may not be visible)
          graph.resize(width, height);
       } else {
          $("#" + divString + " .right .graph").hide();
          $("#" + divString + " .right .table").show();
       }

    });
}


/**
 * Computes the width of y-axis label (so that it does not interfere with values)
 * @param {Object} values in format from the server ([[point, [.,.,.], [.,.,.]], ...])
 */
function getWidthYLabel(values) {
  var realValues = [];

  for (var i = 0; i<values.length; i++) {
      //value of mean + standardDeviation
      realValues.push(values[i][1][2]);
      //value of Q3
      realValues.push(values[i][2][2]);
  }

  var maxVal = Math.max.apply(null, realValues);

  if (maxVal < 10000) {
    return 50;
  } else if (maxVal < 100000) {
    return 60;
  } else if (maxVal < 1000000) {
    return 70;
  } else return 80;
}