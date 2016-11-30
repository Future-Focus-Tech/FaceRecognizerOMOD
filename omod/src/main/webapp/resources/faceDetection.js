
var video = document.getElementById('video');
var canvas = document.getElementById('canvas');
var context = canvas.getContext('2d');
var tracker = new tracking.ObjectTracker('face');

tracker.setInitialScale(4);
tracker.setStepSize(2);
tracker.setEdgesDensity(0.1);
tracking.track('#video', tracker, { camera: true });

var faces = [];
var faceData;
var timeIntervalId;
var getImageFrame = function(){
	if(faceData){
		faces.push(faceData);
        faceData = null;
	}
	if (faces.length == 2){
		clearInterval(timeIntervalId);
        console.log(faces);
        $.ajax({
            type : "POST",
            url : "http://localhost:9090/",
            data : {
                facesData : faces
            },
            success : function(response){
                console.log('successful');
            },
            error : function(e){
                console.log('Error: '+e);
            }
        });
	}
};

tracker.on('track', function(event) {
	if(!timeIntervalId)
		timeIntervalId = setInterval(getImageFrame, 2000);
	context.clearRect(0, 0, canvas.width, canvas.height);
	event.data.forEach(function(rect) {
    	context.drawImage(video, 0, 0, 320, 240);
		context.strokeStyle = '#ff0000';
		context.strokeRect(rect.x, rect.y, rect.width, rect.height);
		context.font = '11px Helvetica';
		context.fillStyle = "#fff";
		context.fillText('x: ' + rect.x + 'px', rect.x + rect.width + 5, rect.y + 11);
		context.fillText('y: ' + rect.y + 'px', rect.x + rect.width + 5, rect.y + 22);
		faceDataFromVideo = context.getImageData(rect.x, rect.y, rect.width, rect.height);
        generateBase64Data(faceDataFromVideo, rect.width, rect.height);
    });
});


var generateBase64Data = function(imageData, width, height){
    var tempCanvas = document.createElement('canvas');
    tempCanvas.width = width;
    tempCanvas.height = height;
    var tempCanvasContext = tempCanvas.getContext('2d');
    tempCanvasContext.putImageData(imageData,0,0);
    faceData = tempCanvas.toDataURL("image/png");
}
