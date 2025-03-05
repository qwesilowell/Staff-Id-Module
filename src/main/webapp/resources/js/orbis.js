/* global PrimeFaces, PF */

var wsUri = "ws://localhost:40000/orbis";

function init() {
    kojakWebSocket();
}

function kojakWebSocket() {
    websocket = new WebSocket(wsUri);
    websocket.onopen = function (evt) {
        onOpen(evt);
    };
    websocket.onclose = function (evt) {
        onClose(evt);
    };
    websocket.onmessage = function (evt) {
        onMessage(evt);
    };
    websocket.onerror = function (evt) {
        onError(evt);
    };
}

function doSend(message) {
    console.log("SENT: " + message);

    if (PF('position').getSelectedValue() === "") {
        alert("Please Select Finger Position");
        return;
    }
    var ghanaCardPin = document.getElementById('ghanaCardPin').value;
    if (ghanaCardPin === "") {
        alert("Please Enter Ghana Card PIN");
        return;
    }

    var obj = new Object();
    obj.action = message;
    obj.docId = generateUUID();
    obj.position = PF('position').getSelectedValue();

    var jsonString = JSON.stringify(obj);
    console.log("SENT: " + jsonString);
    websocket.send(jsonString);
//    const captureDiv = document.querySelector('.blink_me');
//    captureDiv.innerText = "CAPTURE STARTED";
}

function notify() {
//    const growl = PF("fgrowl");
//    if (growl !== null) {
//        console.log("notify triggered");
//        growl.show([{detail: "Scanner opened.", summary: "Scanner opened. Place hand on device"}]);
//    }
}

//function callReloader() {
//    const relr = PF("reloader");
//    if (relr !== null) {
//        console.log("reloader triggered");
//        relr.jq.click();
//    }
//}

function doSendNoId(message) {
    console.log("SENT: " + message);
    var obj = new Object();
    obj.action = message;
    obj.docId = generateUUID();
    var jsonString = JSON.stringify(obj);
    console.log("SENT: " + jsonString);
    websocket.send(jsonString);
    notify();
}

function onOpen(evt) {
    console.log("CONNECTED");
}
function onClose(evt) {
    console.log("DISCONNECTED");
}

function onMessage(evt) {
    var dataObj = JSON.parse(evt.data);
    var total = dataObj.length;
    console.log('DATA ' + evt.data);
    console.log('DATA LENGTH ' + total);

//    DATA {"kojak_message":"Place finger(s) on device"}
    var message = dataObj.kojak_message;
    var dataPresent = dataObj.docId;

    console.log(message);
    if (message) {
        // Update the Growl with the message
        PF('fgrowl').renderMessage({summary: 'Notification', detail: message, severity: 'info'});
    }

    if (dataPresent) {
        PF('fgrowl').renderMessage({summary: 'Notification', detail: "Fingers Captured", severity: 'info'});
        parseFingersFromData(dataObj.finger_data);
    }

    var form = document.theForm || document.getElementById('theForm'); // Try both name and id

    // Log form to ensure it's a valid form element
    console.log(form);

    // Check if form is a valid form element
    if (form && typeof form.submit === 'function') {
//        form.submit(); // Submit the form if it's valid
    } else {
        console.log('Form is not valid or submit method is missing!');
    }
//    callReloader();
//    window.location.reload(true);

}



function storeValue(key, value) {
    if (localStorage) {
        localStorage.setItem(key, value);
    } else {
        $.cookies.set(key, value);
    }
}

function getStoredValue(key) {
    if (localStorage) {
        return localStorage.getItem(key);
    } else {
        return $.cookies.get(key);
    }
}

function generateUUID() {
    var uuid = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx";
    var docId = document.getElementById('socketData').value;
    if (docId === "" || docId.length === 0) {
        var d = new Date().getTime();
        uuid = uuid.replace(/[xy]/g, function (c) {
            var r = (d + Math.random() * 16) % 16 | 0;
            d = Math.floor(d / 16);
            return (c === "x" ? r : (r & 0x3 | 0x8)).toString(16);
        });
        document.getElementById('socketData').value = uuid;
        document.getElementById('socketDataHidden').value = uuid;
        PrimeFaces.ab({source: 'socketData', process: '@this', update: 'socketDataHidden'});
        PrimeFaces.ab({source: 'socketData', process: 'socketDataHidden', update: 'socketDataHidden'});
    } else {
        uuid = docId;
    }
    return uuid;
}
window.addEventListener("load", init, false);

function parseFingersFromData(fingerData) {
    console.log(JSON.parse(fingerData));
    var LLI = document.getElementById("leftLittleImage");
    var LMI = document.getElementById("leftMiddleImage");
    var LRI = document.getElementById("leftRingImage");
    var LII = document.getElementById("leftIndexImage");
    var LL = document.getElementById("leftLittle");
    var LM = document.getElementById("leftMiddle");
    var LR = document.getElementById("leftRing");
    var LI = document.getElementById("leftIndex");

    var RLI = document.getElementById("rightLittleImage");
    var RMI = document.getElementById("rightMiddleImage");
    var RRI = document.getElementById("rightRingImage");
    var RII = document.getElementById("rightIndexImage");
    var RL = document.getElementById("rightLittle");
    var RM = document.getElementById("rightMiddle");
    var RR = document.getElementById("rightRing");
    var RI = document.getElementById("rightIndex");

    var LTI = document.getElementById("leftThumbImage");
    var RTI = document.getElementById("rightThumbImage");
    var RT = document.getElementById("rightThumb");
    var LT = document.getElementById("leftThumb");

    var fingerString = document.getElementById("fingerData");
    var fingerImage = document.getElementById("fingerImage");

    var data = JSON.parse(fingerData);

    if (data.length === 1) {
        fingerImage.src = "data:image/png;base64," + data[0].data;
        fingerString.value = data[0].data;
    } else {
        for (var item in data) {
            if (data[item].hand === "left") {
                switch (data[item].index) {
                    case 0:
                        LLI.src = "data:image/png;base64," + data[item].data;
                        LL.value = data[item].data;
                        break;
                    case 1:
                        LRI.src = "data:image/png;base64," + data[item].data;
                        LR.value = data[item].data;
                        break;
                    case 2:
                        LMI.src = "data:image/png;base64," + data[item].data;
                        LM.value = data[item].data;
                        break;
                    case 3:
                        LII.src = "data:image/png;base64," + data[item].data;
                        LI.value = data[item].data;
                        break;
                }
            } else if (data[item].hand === "right") {
                switch (data[item].index) {
                    case 0:
                        RII.src = "data:image/png;base64," + data[item].data;
                        RI.value = data[item].data;
                        break;
                    case 1:
                        RMI.src = "data:image/png;base64," + data[item].data;
                        RM.value = data[item].data;
                        break;
                    case 2:
                        RRI.src = "data:image/png;base64," + data[item].data;
                        RR.value = data[item].data;
                        break;
                    case 3:
                        RLI.src = "data:image/png;base64," + data[item].data;
                        RL.value = data[item].data;
                        break;
                }
            } else if (data[item].hand === "thumb") {
                switch (data[item].index) {
                    case 0:
                        LTI.src = "data:image/png;base64," + data[item].data;
                        LT.value = data[item].data;
                        break;
                    case 1:
                        RTI.src = "data:image/png;base64," + data[item].data;
                        RT.value = data[item].data;
                        break;

                }
            }
        }
    }
}
