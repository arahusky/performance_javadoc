//code for (range) slider (slightly edited http://jqueryui.com/slider/#range)
$("#slider-range").slider(
{ 
	range: true,
	min: $minValue,
	max: $maxValue,
	step: $step,
	slide: function(event,ui) { 
		if (ui.values[1] - ui.values[0] == 0 ) { 
			$("#amount").val( ui.values[0]); 
		} else { 
			$("#amount").val( ui.values[0] + " to " + ui.values[1] ); 
		};	
	} 
}
);

$("#amount").val( $("#slider-range").slider("values",0) + " to " + $("#slider-range").slider("values",1)); 