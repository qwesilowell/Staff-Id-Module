let capturedImage;

let cropper;
 
function init () {

    console.log(document.querySelector("#faceImage"));

}
 
//Webcam.set({

//    width: '100%',

//    height: '100%',

//    dest_width: 1280,

//    dest_height: 720

//});
 
Webcam.set({

    width: "100%",

    height: "100%",

    dest_width: 889,

    dest_height: 500

});
 
function openCamera (){

    Webcam.attach('#faceImageContainer');

}
 
function capture (){

    Webcam.snap((data_uri) => {

        Webcam.reset();

        document.getElementById('faceImageContainer').innerHTML = `<img src="${data_uri}"/>`;

        const image = document.querySelector('#faceImageContainer img');

        cropper = new Cropper(image, {

            aspectRatio: 0.6933333333333333333,

            crop(event) {
 
            }

        });

    });

}
 
function saveCroppedImage () {

    capturedImage = cropper.getCroppedCanvas().toDataURL();

    document.querySelector("#faceImage").value = capturedImage.replace('data:image/png;base64,', '');

    console.log(document.querySelector("#faceImage").value);

}
 
window.addEventListener("load", init, false);

 