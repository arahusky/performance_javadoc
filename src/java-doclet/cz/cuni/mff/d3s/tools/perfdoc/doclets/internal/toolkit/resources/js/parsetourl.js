function parseToUrl(str) {
	var arr = str.split("#");
	var className = arr[0] + "." + arr[1];
	var methodName = className + "&" + arr[2];

	arrParams = arr[3].split("@");
	var args = "";
	for (i=1; i<arrParams.length; i++) {
		args += "&" + arrParams[i];
	}
	return methodName + args; 
}