
function play(id, url, callback) {
	// cordova bridge
	cordova.exec(function(res){
		if(typeof callback === "function"){
			callback(null,res);
		}
	}
	, null, 'VideoPlayer', 'play', [id, url]);
}
module.exports = play;