function isDivisible(u, d) {
	var numD = Math.max(u.toString().replace(/^\\d+\\./, '').length,
		d.toString().replace(/^\\d+\\./, '').length);
	u = Math.round(u * Math.pow(10, numD));
	d = Math.round(d * Math.pow(10, numD));
	return (u % d) === 0; 
} 
