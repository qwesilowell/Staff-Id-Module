/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */



let cameraOpen = false;

function toggleCamera() {
    const cameraBtn = document.getElementById('cameraButton');
    const captureBtn = document.getElementById('captureButton');
    if (!cameraOpen) {
        openCamera(); // existing camera logic
        captureBtn.style.display = 'inline-block';
        cameraBtn.classList.add('camera-button-red');
        const buttonSpan = cameraBtn.querySelector('.ui-button-text');
        buttonSpan.textContent = 'Reset Camera';
        cameraOpen = true;
    } else {

        openCamera();
    }
}
