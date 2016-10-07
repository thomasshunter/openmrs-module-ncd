// Common javascript methods for the NCD.

function debug(msg) {
	alert(msg);
}

function setDisplayStyle(id, state) {
	document.getElementById(id).style.display = state;
}

function show(id) {
	setDisplayStyle(id, "inline");
}

function hide(id) {
	setDisplayStyle(id, "none");
}

function gotoURL(url) {
	document.location.href=url;
}
