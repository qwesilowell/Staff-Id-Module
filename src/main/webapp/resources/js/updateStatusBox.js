function updateStatusBox() {
    setTimeout(function () {
        var contentBox = document.getElementById('statusBox_content');
        if (!contentBox) {
            console.error('Status box content not found');
            return;
        }
        var status = contentBox.textContent.trim();
        console.log('Raw content: "' + contentBox.textContent + '"');
        console.log('Trimmed status: "' + status + '"');

        var parentBox = document.getElementById('statusBox');
        if (!parentBox) {
            console.error('Status box parent not found');
            return;
        }

        if (status === 'Access Granted') {
            parentBox.className = 'status-box granted';
        } else if (status === 'Access Denied') {
            parentBox.className = 'status-box denied';
        } else {
            parentBox.className = 'status-box';
        }
//        setTimeout(function () {
//            // Reset UI locally
//            parentBox.className = 'status-box';
//            contentBox.textContent = 'Awaiting Action';
//            document.getElementById('ghanaCardPin').value = '';
//            PF('position').selectValue(''); // Reset selectOneMenu
//            PF('entranceWidget').clear();   // Reset autoComplete
//            // Sync server
//            resetFormCommand();
//            console.log('Form reset triggered');
//        }, 10000); // 10 sec delay
    }, 500); // Increased to 500ms
}


