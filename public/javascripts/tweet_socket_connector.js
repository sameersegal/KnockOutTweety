var n = 0;
var socket = null;
function connect(socketUrl, username, password, onConnect, onMessage, onDisconnect) {
	if(socketUrl == null){
		socketUrl = "ws://kot.artoo.in/feed";
	}
	socket = new WebSocket(socketUrl + "?username=" + username + "&password="+password);
	
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
