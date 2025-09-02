
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const soverlay = document.getElementById('sidebar-overlay'); // Updated ID

    console.log('toggleSidebar called, screen width:', window.innerWidth);

    // Toggle sidebar classes (this works for both desktop and mobile)
    sidebar.classList.toggle('collapsed');
    sidebar.classList.toggle('active');

    console.log('Sidebar classes after toggle:', sidebar.className);

    // Only handle overlay for mobile view (screen width <= 768px)
    if (window.innerWidth <= 768) {
        if (sidebar.classList.contains('collapsed')) {
            soverlay.classList.add('show');
            console.log('Sidebar soverlay should now be visible');
        } else {
            soverlay.classList.remove('show');
            console.log('Sidebar soverlay should now be hidden');
        }
        console.log('Sidebar soverlay classes:', soverlay.className);
    }
}

// Initialize elements
const sidebar = document.getElementById("sidebar");
const soverlay = document.getElementById("sidebar-overlay"); // Updated ID

// Close sidebar when overlay is clicked (mobile only)
soverlay.addEventListener("click", () => {
    if (window.innerWidth <= 768) {
        sidebar.classList.remove("collapsed");
        soverlay.classList.remove("show");
    }
});

// Optional: Close sidebar when pressing Escape key (mobile only)
document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && window.innerWidth <= 768 && sidebar.classList.contains("collapsed")) {
        sidebar.classList.remove("collapsed");
        soverlay.classList.remove("show");
    }
});

// Optional: Prevent sidebar from closing when clicking inside it (mobile only)
sidebar.addEventListener("click", (event) => {
    if (window.innerWidth <= 768) {
        event.stopPropagation();
    }
});

// Handle window resize to clean up overlay state
window.addEventListener("resize", () => {
    // If switching from mobile to desktop, hide overlay
    if (window.innerWidth > 768) {
        soverlay.classList.remove("show");
    }
    // If switching from desktop to mobile and sidebar is collapsed, show overlay
    else if (window.innerWidth <= 768 && sidebar.classList.contains("collapsed")) {
        soverlay.classList.add("show");
    }
});

//document.addEventListener('DOMContentLoaded', function () {
//    const icon = document.getElementById('liveMonitoringIcon');
//    // Check if the icon element exists and if the current page's path matches the target URL
//    if (icon && window.location.pathname === '/app/Access/liveMonitoring') {
//        // Add the 'fa-fade' class to start the animation
//        icon.classList.add('fa-fade');
//    }
//});

// Wait for the DOM to be fully loaded
document.addEventListener("DOMContentLoaded", function () {
    // Get the button element
    var imgBtn = document.querySelector('.img-btn');

    if (imgBtn) {
        // Add click event listener
        imgBtn.addEventListener('click', function () {
            // Get the container element
            var cont = document.querySelector('.cont');

            if (cont) {
                // Toggle the class
                cont.classList.toggle('s-signup');
                console.log('Toggle class applied');
            } else {
                console.error('Container element not found');
            }
        });
        console.log('Event listener added to button');
    } else {
        console.error('Button element not found');
    }
});

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

document.addEventListener("DOMContentLoaded", function () {
    var sidebar = document.getElementById("sidebar");
    var mainContent = document.querySelector(".main-content");
//    var footer = document.querySelector(".layout-footer");
    var breadcrumb = document.querySelector(".breadcrumb-container");

    const savedState = localStorage.getItem("sidebarState");

    if (savedState === "open") {
        sidebar.classList.add("active");
        mainContent.classList.add("sidebar-active");
//        footer.classList.add("sidebar-active");
        breadcrumb.classList.add("sidebar-active");
    } else {
        sidebar.classList.remove("active");
        mainContent.classList.remove("sidebar-active");
//        footer.classList.remove("sidebar-active");
        breadcrumb.classList.remove("sidebar-active");
    }

    //  DO NOT add 'animated' class here
});





function toggleDropdown() {
    var dropdown = document.getElementById("dropdownContent");
    if (dropdown) {
        dropdown.style.display = dropdown.style.display === "block" ? "none" : "block";
    }
}

function toggleNotifications() {
    const sidebar = document.getElementById('notificationSidebar');
    const overlay = document.getElementById('overlay');

    sidebar.classList.add('open');
    overlay.classList.add('active');
}

// Close notification sidebar
function closeNotifications() {
    const sidebar = document.getElementById('notificationSidebar');
    const overlay = document.getElementById('overlay');

    sidebar.classList.remove('open');
    overlay.classList.remove('active');
}

// Close sidebar on Escape key
document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
        closeNotifications();
    }
});

function addMarkingClass(button) {
    try {
        var buttonElement = document.getElementById(button.id);
        if (buttonElement)
            var notificationItem = buttonElement.closest('.notification-item');
        if (notificationItem) {
            notificationItem.classList.add('marking-read');

            // Ensure animation is visible for minimum duration
            setTimeout(function () {
                console.log('Mark as read animation guaranteed to show');
            }, 400);
        }

    } catch (e) {
        console.log('Animation error (non-critical):', e);
    }
    return true; // Always allow AJAX to proceed
}
function removeMarkingClass() {
    // Remove marking-read class after successful update
    setTimeout(function () {
        var markingElements = document.querySelectorAll('.marking-read');
        markingElements.forEach(function (el) {
            el.classList.remove('marking-read');
        });
    }, 500);
}


function addDismissingClass(button) {
    try {
        var buttonElement = document.getElementById(button.id);
        if (buttonElement) {
            var notificationItem = buttonElement.closest('.notification-item');
            if (notificationItem) {
                notificationItem.classList.add('dismissing');

                setTimeout(function () {
                    console.log('Dismiss animation guaranteed to complete');
                }, 600);
            }
        }
    } catch (e) {
        console.log('Animation error (non-critical):', e);
    }
    return true; // Always allow AJAX to proceed
}
function removeDismissingClass() {
    // This will be called after the AJAX update completes
    // The dismissed item should already be removed from DOM by the update
    setTimeout(function () {
        var dismissingElements = document.querySelectorAll('.dismissing');
        dismissingElements.forEach(function (el) {
            el.classList.remove('dismissing');
        });
    }, 700);
}



let previousNotificationCount = 0;

// Initialize sound and get initial count when page loads
document.addEventListener('DOMContentLoaded', function () {
    // Create sound object
    console.log('Audio path:', notificationSound.src);
    notificationSound.volume = 0.9;

// Get initial count (so we don't play sound on page load)
    const badgeElement = document.querySelector('#notificationBadge');
    if (badgeElement && badgeElement.textContent) {
        previousNotificationCount = parseInt(badgeElement.textContent.replace('+', '')) || 0;
        console.log(previousNotificationCount);
    }
}
);

// This function will be called after each poll update
function checkForNewNotifications()
{
    const badgeElement = document.querySelector('#notificationBadge');
    if (badgeElement) {
        const currentCountText = badgeElement.textContent || '0';
        const currentCount = parseInt(currentCountText.replace('+', '')) || 0;

        // If count increased, play sound
        if (currentCount > previousNotificationCount && previousNotificationCount >= 0) {
            playNotificationSound();
        }

        // Update previous count
        previousNotificationCount = currentCount;
    }
}


function playNotificationSound() {
    try {
        if (notificationSound) {
            notificationSound.currentTime = 0; // Reset to beginning
            notificationSound.play().catch(error => {
                console.log('Could not play notification sound:', error);
            });
        }
    } catch (error) {
        console.log('Audio not supported:', error);
    }
}
