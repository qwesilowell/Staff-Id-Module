function updateStatusBox() {
    setTimeout(function () {
        var contentBox = document.getElementById('statusBox_content');
        if (!contentBox)
            return;

        var status = contentBox.textContent.trim();
        var parentBox = document.getElementById('statusBox');
        if (!parentBox)
            return;

        // Update status box appearance
        if (status === 'Access Granted') {
            parentBox.className = 'status-box granted';
        } else if (status === 'Access Denied') {
            parentBox.className = 'status-box denied';
        } else {
            parentBox.className = 'status-box';
        }

        // Reset UI after delay
        setTimeout(function () {
            // Reset status
            parentBox.className = 'status-box';
            contentBox.textContent = 'Awaiting Action';

            // Reset input fields
            if (document.getElementById('ghanaCardPin')) {
                document.getElementById('ghanaCardPin').value = '';
            }

            // Reset dropdown
            try {
                if (typeof PF === 'function' && PF('position')) {
                    PF('position').selectValue('');
                }
            } catch (e) {
            }

            // Reset entrance field - direct DOM approach
            var entranceInput = document.querySelector('.ui-autocomplete-input');
            if (entranceInput) {
                entranceInput.value = '';
            }

            // Clear dropdown panels
            var panels = document.querySelectorAll('.ui-autocomplete-panel');
            for (var i = 0; i < panels.length; i++) {
                panels[i].style.display = 'none';
            }

            // Sync with server
            if (typeof resetFormCommand === 'function') {
                resetFormCommand();
            }
        }, 10000); // 10 sec delay
    }, 500);
}
