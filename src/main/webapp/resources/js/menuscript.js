function toggleSidebar() {
    var sidebar = document.getElementById("sidebar");
    if (sidebar) {
        sidebar.classList.toggle("active");
    }
}

function toggleDropdown() {
    var dropdown = document.getElementById("dropdownContent");
    if (dropdown) {
        dropdown.style.display = dropdown.style.display === "block" ? "none" : "block";
    }
}

// Close the dropdown when clicking outside
document.addEventListener("click", function (event) {
    var dropdown = document.getElementById("dropdownContent");
    var profile = document.querySelector(".profile-dropdown");
    // Only proceed if both elements exist
    if (dropdown && profile) {
        if (!profile.contains(event.target)) {
            dropdown.style.display = "none";
        }
    }
});
