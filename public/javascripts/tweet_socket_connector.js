var n = 0;
var socket = null;
function connect(callback) {
	socket = new WebSocket("ws://localhost:9000/feed");

	socket.onmessage = function(event) {
		//var json = eval('(' + event.data + ')');
		//if (json["geo"] != null) {
		//	$("div#messages").prepend(
		//			'<br /><span class="badge badge-info">' + ++n
		//					+ '</span><br />' + json["created_at"] + " : "
		//					+ json["text"] + '<br /><hr />');
		//}
		callback(eval('(' + event.data + ')'));
	}

	socket.onclose = function(event) {
		socket = null;
		// connect();
	}
}