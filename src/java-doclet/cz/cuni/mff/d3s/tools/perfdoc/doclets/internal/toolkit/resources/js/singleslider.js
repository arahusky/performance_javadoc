//code for (single) slider (http://jqueryui.com/slider/#range) 
$("#slider-range").slider(
{ 
	min: $minValue,
	max: $maxValue,
	step: $step,
	slide: function(event, ui) {
		$("#amount").val(ui.value); 
	} 
}
); 

$("#amount").val( $("#slider-range").slider("value") );  