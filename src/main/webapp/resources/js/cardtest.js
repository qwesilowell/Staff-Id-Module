/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */


let socket = null;

function log(message) {
    const consoleLog = document.getElementById('consoleLog');
    const timestamp = new Date().toLocaleTimeString();
    consoleLog.innerHTML += `[${timestamp}] ${message}\n`;
    consoleLog.scrollTop = consoleLog.scrollHeight;
    console.log(message);
}

function updateStatus(status, isConnected) {
    const statusElement = document.getElementById('statusText');
    const statusContainer = document.getElementById('connectionStatus');
    statusElement.textContent = status;
    if (isConnected) {
        statusContainer.style.background = '#d4edda';
        statusContainer.style.color = '#155724';
    } else {
        statusContainer.style.background = '#f8d7da';
        statusContainer.style.color = '#721c24';
    }
}

function showError(errorMsg) {
    const errorContainer = document.getElementById('errorContainer');
    const errorMessage = document.getElementById('errorMessage');
    errorMessage.textContent = errorMsg;
    errorContainer.style.display = 'block';
    setTimeout(() => {
        errorContainer.style.display = 'none';
    }, 5000);
}

function displayCardData(data) {
    document.getElementById('firstName').textContent = data.firstName || '-';
    document.getElementById('surname').textContent = data.surname || '-';
    document.getElementById('cardPin').textContent = data.ghanaCardPin || '-';
    document.getElementById('gender').textContent = data.gender || '-';
    document.getElementById('cardDataContainer').style.display = 'block';
}

function connectWebSocket() {
    if (socket && socket.readyState === WebSocket.OPEN) {
        log('Already connected to WebSocket server');
        return;
    }

    log('Connecting to card reader server...');
    updateStatus('Connecting...', false);
    // Connect to CardAgent server
    socket = new WebSocket('ws://localhost:8081/card');
    socket.onopen = function () {
        log(' Connected to card reader server');
        updateStatus('Connected', true);
    };
    socket.onmessage = function (event) {
        log(' Received: ' + event.data);
        try {
            const data = JSON.parse(event.data);
            if (data.error) {
                log(' Error: ' + data.error);
                showError(data.error);
            } else if (data.debug) {
                log("Reader Debug Info:\n" + data.debug);
            } else {
                log(' Card data received successfully');
                displayCardData(data)
            }
        } catch (e) {
            log(' Error parsing response: ' + e.message);
            showError('Invalid response from server');
        }
    };
    socket.onerror = function (error) {
        log(' WebSocket error: ' + error);
        updateStatus('Error', false);
        showError('Connection error. Make sure CardAgent server is running.');
    };
    socket.onclose = function () {
        log(' Connection closed');
        updateStatus('Disconnected', false);
    };
}

function readCard() {
    if (!socket || socket.readyState !== WebSocket.OPEN) {
        log(' Not connected to server. Attempting to connect...');
        connectWebSocket();
        setTimeout(() => {
            if (socket && socket.readyState === WebSocket.OPEN) {
                readCard();
            } else {
                showError('Please connect to the card reader first');
            }
        }, 1000);
        return;
    }


    log(' Sending readCard command with CAN ' + can + '...');
    const request = {action: 'readCard', can: can};
    socket.send(JSON.stringify(request));
}

function debugReaders() {
    if (!socket || socket.readyState !== WebSocket.OPEN) {
        log("Not connected. Please connect first.");
        return;
    }
    log("Requesting reader debug info...");
    socket.send(JSON.stringify({action: "debugReaders"}));
}


// Auto-connect when page loads
window.addEventListener('load', function () {
    log('Page loaded. Auto-connecting to card reader...');
    setTimeout(connectWebSocket, 500);
});

