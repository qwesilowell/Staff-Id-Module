/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */
function focusPasswordModal() {
    setTimeout(function () {
        var modal = document.getElementById('passwordChangeModal');
        if (modal && modal.style.display !== 'none') {
            var firstInput = modal.querySelector('input[type="password"]');
            if (firstInput) {
                firstInput.focus();
            }
        }
    }, 100);
}


