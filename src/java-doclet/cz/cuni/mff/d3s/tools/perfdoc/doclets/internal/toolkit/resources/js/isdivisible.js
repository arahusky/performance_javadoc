/**
 * Determines whether first parameter is divisible by second one
 * @param {Number} u dividend
 * @param {Number} d divisor
 * @return {Boolean} true if (u % d) == 0
 */
function isDivisible(u, d) {
	var numD = Math.max(u.toString().replace(/^\\d+\\./, '').length,
		d.toString().replace(/^\\d+\\./, '').length);
	u = Math.round(u * Math.pow(10, numD));
	d = Math.round(d * Math.pow(10, numD));
	return (u % d) === 0; 
} 
