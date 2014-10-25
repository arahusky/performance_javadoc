/**
 * Determines whether value can be reached by adding step to min
 * @param {Number} value actual value
 * @param {Number} min minimal value
 * @param {Number} step 
 * @return {boolean} true if value = min + x * step (for x integer)
 */
function stepCheck(value, min, step) {
	var accuracy = step.toString().replace(/^\\d+\\./, '').length;
    var newValue = Math.round((value - min) * Math.pow(10, accuracy));
    var newStep = Math.round(step * Math.pow(10, accuracy));

    return (newValue % newStep === 0);
}