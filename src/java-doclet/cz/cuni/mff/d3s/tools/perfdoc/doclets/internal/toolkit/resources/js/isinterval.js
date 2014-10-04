function isInterval(str, min, max, step) {
	//if there's no " to " separator, it can not be an interval
	if (str.indexOf(" to ") == -1) return "false"; 

	var array = str.split(" to ");
	if (array.length != 2) return "false"; 

	if (isNaN(array[0]) || isNaN(array[1])) return "false";

	if (((array[0] * 1) < min) || ((array[1] * 1) > max) || ((array[0] * 1) > (array[1] * 1))) return "false";

	if (! (isDivisible(array[0] - min, step) && isDivisible(array[1] - min, step))) return "false";

	//if it contains to, but left and right numbers are equal, it is so called single
	if (array[0] == array[1]) return "single";

	//otherwise, it is an interval
	return "true"; 
} 