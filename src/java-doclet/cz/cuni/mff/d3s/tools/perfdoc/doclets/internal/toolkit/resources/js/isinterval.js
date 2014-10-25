/**
 * Determines whether str represents correct interval (reachable by adding step to min and not bigger than max), or just correct number, 
 * or is just some silly string
 * @param {string} str string to be checked for being an interval
 * @param {Number} min the minimal allowed value
 * @param {Number} max the maximal allowed value
 * @param {Number} step 
 * @return {string} "true" if str represents correct interval (e.g. "15 to 30"), "single" if it represents just one number (e.g. "15"), otherwise "false"
 */
 function isInterval(str, min, max, step) {
	//if there's no " to " separator, it can not be an interval
	if (str.indexOf(" to ") == -1) return "false"; 

	var array = str.split(" to ");
	if (array.length != 2) return "false"; 

	if (isNaN(array[0]) || isNaN(array[1])) return "false";

	if (((array[0] * 1) < min) || ((array[1] * 1) > max) || ((array[0] * 1) > (array[1] * 1))) return "false";

	if (! (stepCheck(array[0], min, step) && stepCheck(array[1], min, step))) return "false";

	//if it contains " to ", but left and right numbers are equal, it is so called single
	if (array[0] == array[1]) return "single";

	//otherwise, it is an interval
	return "true"; 
} 
