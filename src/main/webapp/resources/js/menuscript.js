//function toggleSidebar() {
//    var sidebar = document.getElementById("sidebar");
//    var mainContent = document.querySelector(".main-content");
//    var footer = document.querySelector(".layout-footer");
//    var breadcrumb = document.querySelector(".breadcrumb-container");
//
//    if (sidebar) {
//        sidebar.classList.toggle("active");
//        mainContent.classList.toggle("sidebar-active");
//        footer.classList.toggle("sidebar-active");
//        breadcrumb.classList.toggle("sidebar-active");
//    }
//}

function toggleSidebar() {
    var sidebar = document.getElementById("sidebar");
    var mainContent = document.querySelector(".main-content");
//    var footer = document.querySelector(".layout-footer");
    var breadcrumb = document.querySelector(".breadcrumb-container");

    if (sidebar) {
        sidebar.classList.toggle("active");
        mainContent.classList.toggle("sidebar-active");
//        footer.classList.toggle("sidebar-active");
        breadcrumb.classList.toggle("sidebar-active");

        // Save the current state to localStorage
        if (sidebar.classList.contains("active")) {
            localStorage.setItem("sidebarState", "open");
        } else {
            localStorage.setItem("sidebarState", "closed");
        }
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

// Close the dropdown when clicking outside (Works perfectly just uncomment)
//document.addEventListener("click", function (event) {
//    var sidebar = document.getElementById("sidebar");
//    var toggleBtn = document.querySelector(".toggle-button");
//
//    if (
//            sidebar &&
//            !sidebar.contains(event.target) &&
//            !toggleBtn.contains(event.target)
//            ) {
//        sidebar.classList.remove("active");
//
//        // Also remove the layout classes
//        document.querySelector(".main-content").classList.remove("sidebar-active");
//        document.querySelector(".layout-footer").classList.remove("sidebar-active");
//        document.querySelector(".breadcrumb-container").classList.remove("sidebar-active");
//    }
//});

