var n = 0;
var socket = null;
function connect(username, password, onConnect, onMessage, onDisconnect) {
	socket = new WebSocket("ws://localhost:9000/feed?username=" + username + "&password="+password);
	
	socket.onopen = function(event) {
		onConnect(event);
	}

	socket.onmessage = function(event) {
		onMessage(eval('(' + event.data + ')'));
	}

	socket.onclose = function(event) {
		socket = null;
		onDisconnect(event);
	}

	return socket;
}
